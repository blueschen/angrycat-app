package com.angrycat.erp.web.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.angrycat.erp.model.Exam;
import com.angrycat.erp.model.ExamItem;
import com.angrycat.erp.service.RandomExamService;
import com.angrycat.erp.web.WebUtils;

@Controller
@RequestMapping("/test")
@Scope("session")
public class TestController {
	@Autowired
	private RandomExamService randomExamService;
	
	@RequestMapping(value="/",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public String view(Model model){
		List<Exam> exams = setExamGroup();
		model.addAttribute("showExams", exams);
		// TODO 顯示考生之前的分數統計
		return "test/view";
	}
	private List<Exam> copy(List<Exam> exams){
		List<Exam> copyExams = 
			exams.stream().map(e->{
				List<ExamItem> copyItems
					= e.getItems().stream().map(ei->{
						ExamItem copyItem = new ExamItem();
						copyItem.setSequence(ei.getSequence());
						copyItem.setDescription(ei.getDescription());
						// 不複製是否為正確答案，避免將答案提示傳到前端
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
