package com.angrycat.erp.web.controller;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
		List<Exam> exams = setExamGroup();
		model.addAttribute("showExams", CommonUtil.parseToJson(exams));
		// TODO 顯示考生之前的分數統計
		return "test/view";
	}
	@RequestMapping(value="/retest",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String retest(){
		List<Exam> exams = setExamGroup();
		String result = CommonUtil.parseToJson(exams);
		return result;
	}
	@RequestMapping(value="/score",
			method={RequestMethod.POST, RequestMethod.GET},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String score(@RequestBody List<Exam> exams){
		List<Exam> originalExams = (List<Exam>)WebUtils.currentSession().getAttribute("originalExams");
		Map<String, List<String>> answers = 
			originalExams
				.stream()
				.collect(Collectors.toMap(
					Exam::getId, 
					p->p.getItems()
						.stream()
						.filter(i->i.isCorrect())
						.map(i->i.getDescription())
						.collect(Collectors.toList())));
		int total = 100;
		int partition = total / originalExams.size();
		int score = exams.stream().mapToInt(e->{
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
			Date today = ts.atStartOfToday();
			User user = WebUtils.getSessionUser();
			String queryStatistics = "SELECT es FROM " + ExamStatistics.class.getName() + " es WHERE es.examDate = :today AND es.examinee = :user";
			List<ExamStatistics> results = s.createQuery(queryStatistics).setDate("today", today).setEntity("user", user).list();
			
			if(results.isEmpty()){
				es = new ExamStatistics();
				es.setExamDate(today);
				es.setExaminee(user);
				s.save(es);
				s.flush();
				System.out.println("initialized statistics id:"+ es.getId());
			}else{
				es = results.get(0);
			}
			
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
			
			scores = s.createQuery("SELECT es.score FROM " + ExamScore.class.getName() + " es WHERE es.statistics.id = :id ORDER BY es.id DESC").setString("id", es.getId()).list();
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
		info.put("corrected", exams);
		info.put("score", score);
		info.put("scores", scores);
		info.put("statistics", es);
		
		String result = CommonUtil.parseToJson(info);
		return result;
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
				return copyExam;
			}).collect(Collectors.toList());
		return copyExams;
	}
	public List<Exam> setExamGroup(){
		List<Exam> exams = randomExamService.setExamGroup();
		WebUtils.currentSession().setAttribute("originalExams", exams);
		List<Exam> copyExams = copy(exams);
		return copyExams;
	}	
}
