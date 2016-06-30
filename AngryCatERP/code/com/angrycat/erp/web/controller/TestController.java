package com.angrycat.erp.web.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.poi.util.IOUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.model.Exam;
import com.angrycat.erp.model.ExamItem;
import com.angrycat.erp.model.ExamScore;
import com.angrycat.erp.model.ExamStatistics;
import com.angrycat.erp.security.User;
import com.angrycat.erp.service.RandomExamService;
import com.angrycat.erp.service.TimeService;
import com.angrycat.erp.web.WebUtils;


@Controller
@RequestMapping("/test")
@Scope("session")
public class TestController {
	private static final String ANSWERS = "answers";
	private static final String EXAMS = "exams";
	private static final String CURRENT_EXAM_IDX = "currentExamIdx";
	private static final String REPLIES = "replies";
	
	@Autowired
	private RandomExamService randomExamService;
	@Autowired
	private SessionFactoryWrapper sfw;
	@Autowired
	private TimeService ts;
	
	@RequestMapping(value="/execute",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public String view(Model model){
//		List<Exam> exams = setExamGroup();
//		model.addAttribute("showExams", CommonUtil.parseToJson(exams));
		List<ExamStatistics> found = 
			sfw.executeSession(s->{
				ExamStatistics result = findTodayStatistics(s);
				return Arrays.asList(result);
			});
		ExamStatistics es = found.get(0);
		es.setExaminee(null);
		List<Integer> scores = 
			sfw.executeSession(s->{
				List<Integer> results = findTodayScores(s, es.getId());
				return results;
			});
		Map<String, Object> info = new LinkedHashMap<>();
		info.put("scores", scores);
		info.put("statistics", es);
		
		model.addAttribute("info", CommonUtil.parseToJson(info));
		return "test/view";
	}
	@RequestMapping(value="/startTest",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String startTest(){
		HttpSession s= WebUtils.currentSession();
		s.setAttribute(CURRENT_EXAM_IDX, null);
		s.setAttribute(REPLIES, null);
		
		List<Exam> exams = setExamGroup();
		Exam exam = exams.get(0);
		int examNum = exams.size(); // 題數
		Map<String, Object> info = new LinkedHashMap<>();
		info.put("firstExam", exam);
		info.put("examNum", examNum);
		String result = CommonUtil.parseToJson(info);
		return result;
	}
	@RequestMapping(value="/correctAfterReply",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String correctAfterReply(@RequestBody Exam reply){
		List<Exam> replies = getReplies();
		replies.add(reply);
		Exam answer = getAnswer();
		IntStream.range(0, reply.getItems().size()).boxed()
			.forEach(i->{
				answer.getItems().get(i).setSelected(reply.getItems().get(i).isSelected());
			});
		String result = CommonUtil.parseToJson(answer);
		return result;
	}
	@RequestMapping(value="/nextExam",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String nextExam(){
		Exam nextExam = getNextExam();
		String result = CommonUtil.parseToJson(nextExam);
		return result;
	}
	@RequestMapping(value="/score",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String score(){
		List<Exam> answerExams = (List<Exam>)WebUtils.currentSession().getAttribute(ANSWERS);
		Map<String, List<String>> answers = 
			answerExams
				.stream()
				.collect(Collectors.toMap(
					Exam::getId, 
					p->p.getItems()
						.stream()
						.filter(i->i.isCorrect())
						.map(i->i.getDescription())
						.collect(Collectors.toList())));
		int total = 100;
		int partition = total / answerExams.size();
		
		List<Exam> replies = getReplies();
		int score = replies.stream().mapToInt(e->{
			List<String> descriptions = answers.get(e.getId()); // 找到正確解答的題項描述
			e.getItems().stream().forEach(i->{
				if(descriptions.contains(i.getDescription())){
					i.setCorrect(true); // 該題項標示為正確答案
				}
			});
			
			List<String> responses = e.getItems().stream().filter(i->i.isSelected()).map(i->i.getDescription()).collect(Collectors.toList());
			if(descriptions.containsAll(responses)
			&& descriptions.size() == responses.size()){
				return partition;
			}
			return 0;
		}).sum();
		
		// 計分之後，要轉成統計數據儲存至資料庫
		Session s = sfw.openSession();
		Transaction tx = s.beginTransaction();
		ExamStatistics es = null;
		List<Integer> scores = null;
		try{
			es = findTodayStatistics(s);
			
			ExamScore scoring = new ExamScore();
			scoring.setScore(score);
			scoring.setStatistics(es);
			s.save(scoring);
			s.flush();
			
			String queryScores = "SELECT MAX(es.score), MIN(es.score), AVG(es.score), COUNT(es.id) FROM " + ExamScore.class.getName() + " es WHERE es.statistics.id = :id";
			Object[] statistics = (Object[])s.createQuery(queryScores).setString("id", es.getId()).uniqueResult();
			int max = (Integer)statistics[0];
			int min = (Integer)statistics[1];
			double avg = (Double)statistics[2];
			long count = (Long)statistics[3];
			
			es.setMaxScore(max);
			es.setAvgScore((int)avg); // 直接轉型，無條件捨去
			es.setExamCount((int)count);
			
			s.update(es);
			s.flush();
			
			scores = findTodayScores(s, es.getId());
			s.clear();
			tx.commit();
		}catch(Throwable e){
			tx.rollback();
			throw new RuntimeException(e);
		}finally{
			s.close();
		};	
		
		es.setExaminee(null);
		Map<String, Object> info = new LinkedHashMap<>();
		info.put("scores", scores);
		info.put("statistics", es);
		
		String result = CommonUtil.parseToJson(info);
		return result;
	}
	@RequestMapping(
			value="/downloadImage/{imgPath}",
			method={RequestMethod.GET, RequestMethod.POST})
	public void downloadImage(@PathVariable("imgPath") String imgPath, HttpServletResponse res){
		res.setContentType("image/jpeg");
		File f = randomExamService.getArchives().get(imgPath);
		if(f == null){
			return;
		}
		try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			OutputStream os = res.getOutputStream();){
			IOUtils.copy(bis, os);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	private List<Exam> copy(List<Exam> exams){
		List<Exam> copyExams = 
			exams.stream().map(e->{
				List<ExamItem> copyItems
					= e.getItems().stream().map(ei->{
						ExamItem copyItem = new ExamItem();
						copyItem.setSequence(ei.getSequence());
						copyItem.setDescription(ei.getDescription());
//						copyItem.setCorrect(ei.isCorrect());// 不複製是否為正確答案，避免將答案提示傳到前端
						return copyItem;
					}).collect(Collectors.toList());
								
				Exam copyExam = new Exam();
				copyExam.setId(e.getId());
				copyExam.setCategory(e.getCategory());
				copyExam.setCreateDate(e.getCreateDate());
				copyExam.setDescription(e.getDescription());
				copyExam.setHint(e.getHint());
				copyExam.setItems(copyItems);
				copyExam.setTopicImaged(e.isTopicImaged());
				copyExam.setQuestionImaged(e.isQuestionImaged());
				return copyExam;
			}).collect(Collectors.toList());
		return copyExams;
	}
	public List<Exam> setExamGroup(){
		List<Exam> answerExams = randomExamService.setExamGroup();
		WebUtils.currentSession().setAttribute(ANSWERS, answerExams);
		List<Exam> exams = copy(answerExams);
		WebUtils.currentSession().setAttribute(EXAMS, exams);
		return exams;
	}
	private Exam getNextExam(){
		int idx = getCurrentExamIdx();
		int nextIdx = ++idx;
		HttpSession s = WebUtils.currentSession();
		List<Exam> exams = (List<Exam>)s.getAttribute(EXAMS);
		Exam nextExam = exams.get(nextIdx);
		s.setAttribute(CURRENT_EXAM_IDX, nextIdx);
		return nextExam;
	}
	private Exam getAnswer(){
		int idx = getCurrentExamIdx();
		List<Exam> originalExams = (List<Exam>)WebUtils.currentSession().getAttribute(ANSWERS);
		return originalExams.get(idx);
	}
	private int getCurrentExamIdx(){
		HttpSession s = WebUtils.currentSession();
		if(s.getAttribute(CURRENT_EXAM_IDX)==null){
			s.setAttribute(CURRENT_EXAM_IDX ,0);
		}
		int idx = (int)s.getAttribute(CURRENT_EXAM_IDX);
		return idx;
	}
	private List<Exam> getReplies(){
		HttpSession s = WebUtils.currentSession();
		List<Exam> replies = (List<Exam>)s.getAttribute(REPLIES);
		if(replies==null){
			replies = new LinkedList<>();
			s.setAttribute(REPLIES ,replies);
		}
		return replies;
	}
	private ExamStatistics findTodayStatistics(Session s){
		Date today = ts.atStartOfToday();
		User user = WebUtils.getSessionUser();
		String queryStatistics = "SELECT es FROM " + ExamStatistics.class.getName() + " es WHERE es.examDate = :today AND es.examinee = :user";
		List<ExamStatistics> results = s.createQuery(queryStatistics).setDate("today", today).setEntity("user", user).list();
		ExamStatistics es = null;
		if(results.isEmpty()){
			es = new ExamStatistics();
			es.setExamDate(ts.atStartOfToday());
			es.setExaminee(WebUtils.getSessionUser());
			s.save(es);
			s.flush();
			System.out.println("initialized statistics id:"+ es.getId());
		}else{
			es = results.get(0);
		}
		return es;
	}
	private List<Integer> findTodayScores(Session s, String statisticsId){
		List<Integer> scores = s.createQuery("SELECT es.score FROM " + ExamScore.class.getName() + " es WHERE es.statistics.id = :id ORDER BY es.id DESC").setString("id", statisticsId).list();
		return scores;
	}
}
