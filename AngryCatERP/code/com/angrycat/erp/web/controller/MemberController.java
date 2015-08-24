package com.angrycat.erp.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.angrycat.erp.businessrule.MemberVipDiscount;
import com.angrycat.erp.businessrule.VipDiscountUseStatus;
import com.angrycat.erp.common.CommonUtil;
import com.angrycat.erp.component.SessionFactoryWrapper;
import com.angrycat.erp.condition.ConditionFactory;
import com.angrycat.erp.condition.MatchMode;
import com.angrycat.erp.excel.ExcelExporter;
import com.angrycat.erp.excel.ExcelImporter;
import com.angrycat.erp.jackson.mixin.MemberIgnoreDetail;
import com.angrycat.erp.model.Member;
import com.angrycat.erp.model.VipDiscountDetail;
import com.angrycat.erp.service.CrudBaseService;
import com.angrycat.erp.web.WebUtils;
import com.angrycat.erp.web.component.ConditionConfig;



@Controller
@RequestMapping(value="/member")
@Scope("session")
public class MemberController {
	@Autowired
	@Qualifier("crudBaseService")
	private CrudBaseService<Member, Member> memberCrudService;
	
	@Autowired
	@Qualifier("crudBaseService")
	private CrudBaseService<Member, Member> findMemberService;
	
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@Autowired
	private ExcelImporter excelImporter;
	
	@Autowired
	private ExcelExporter excelExporter;
	
	@Autowired
	private MemberVipDiscount discount;
	@Autowired
	private VipDiscountUseStatus useStatus;
	
	@PostConstruct
	public void init(){
		memberCrudService.setRootAndInitDefault(Member.class);
		
		String root = CrudBaseService.DEFAULT_ROOT_ALIAS;
		String rootAliasWith = root + ".";
		memberCrudService
			.addWhere(ConditionFactory.putStrCaseInsensitive(rootAliasWith+"name LIKE :pName", MatchMode.ANYWHERE))
			.addWhere(ConditionFactory.putInt(rootAliasWith+"gender=:pGender"))
			.addWhere(ConditionFactory.putSqlDate(rootAliasWith+"birthday >= :pBirthdayStart"))
			.addWhere(ConditionFactory.putSqlDate(rootAliasWith+"birthday <= :pBirthdayEnd"))
			.addWhere(ConditionFactory.putStr(rootAliasWith+"idNo LIKE :pIdNo", MatchMode.START))
			.addWhere(ConditionFactory.putStrCaseInsensitive(rootAliasWith+"fbNickname LIKE :pFbNickname", MatchMode.ANYWHERE))
			.addWhere(ConditionFactory.putStr(rootAliasWith+"mobile LIKE :pMobile", MatchMode.START))
			.addWhere(ConditionFactory.putBoolean(rootAliasWith+"important = :pImportant"))
		;
		memberCrudService.setUser(WebUtils.getSessionUser());
		
		findMemberService
			.createFromAlias(Member.class.getName(), root)
			.createAssociationAlias("left join fetch "+rootAliasWith+"vipDiscountDetails", "details", null)
			.addWhere(ConditionFactory.putStr(rootAliasWith+"id = :pId"));
		findMemberService.setUser(WebUtils.getSessionUser());
	}
	
	@RequestMapping(value="/list", method=RequestMethod.GET)
	public String list(){
		System.out.println("list.... ");
		return "member/list";
	}
	
	@RequestMapping(value="/queryAll", 
			method=RequestMethod.GET,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody String queryAll(){
		System.out.println("queryAll.... ");
		ConditionConfig<Member> cc = memberCrudService.genCondtitionsAfterExecuteQueryPageable();
		String result = memberIgnoreDetail(cc);
		return result;
	}
	
	private String memberIgnoreDetail(Object obj){
		String result = CommonUtil.parseToJson(obj, Member.class, MemberIgnoreDetail.class);
		return result;
	}
	
	@RequestMapping(value="/queryCondtional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryCondtional(@RequestBody ConditionConfig<Member> conditionConfig){
		ConditionConfig<Member> cc = memberCrudService.executeQueryPageable(conditionConfig);
		String result = memberIgnoreDetail(cc);
		return result;
	}
	
	@RequestMapping(value="/deleteItems",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String deleteItems(@RequestBody List<String> ids){
		ConditionConfig<Member> cc = memberCrudService.executeQueryPageableAfterDelete(ids);
		String result = memberIgnoreDetail(cc);
		return result;
	}
	@RequestMapping(value="/add",
			method=RequestMethod.GET)
	public String add(){
		return "member/view";
	}
	@RequestMapping(value="/view/{id}",
			method=RequestMethod.GET)
	public String view(@PathVariable("id")String id, Model model){
		System.out.println("call view id: " + id);
		findMemberService.getSimpleExpressions().get("pId").setValue(id);
		List<Member> members = findMemberService.executeQueryList();
		Member member = null;
		if(!members.isEmpty()){
			member = members.get(0);
		}
		if(member!=null){
			useStatus.applyRule(member);
		}
		String result = CommonUtil.parseToJson(member);
		model.addAttribute("member", result);
		return "member/view";
	}
	@RequestMapping(value="/save",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody Member saveOrMerge(@RequestBody Member member){
		sfw.executeSaveOrUpdate(s->{
			if(StringUtils.isBlank(member.getId())){
				if(member.getVipDiscountDetails().size() > 0){// 連同明細一起新增
					List<VipDiscountDetail> detail = member.getVipDiscountDetails();
					member.setVipDiscountDetails(new LinkedList<VipDiscountDetail>());
					s.save(member);
					s.flush();
					detail.stream().forEach(d->{
						d.setMemberId(member.getId());
					});
					member.getVipDiscountDetails().addAll(detail);
				}
			}else{
				findMemberService.getSimpleExpressions().get("pId").setValue(member.getId());
				List<Member> members = findMemberService.executeQueryList();
				
				if(!members.isEmpty()){
					Member sessionMember = members.get(0);
					Iterator<VipDiscountDetail> details = sessionMember.getVipDiscountDetails().iterator();
					boolean deleted = false;
					while(details.hasNext()){
						boolean deleting = true;
						VipDiscountDetail detail = details.next();
						for(VipDiscountDetail d : member.getVipDiscountDetails()){
							if(detail.getId().equals(d.getId())){
								deleting = false;
								break;
							}
						}
						if(deleting){
							details.remove();
							deleted = true;
						}
					}
					if(deleted){
						s.saveOrUpdate(sessionMember);
						s.flush();
					}
					s.evict(sessionMember);
				}
			}				
			s.saveOrUpdate(member);
			s.flush();
		});
		return member;
	}
	
	@RequestMapping(
			value="/uploadExcel", 
			method=RequestMethod.POST, 
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String uploadExcel(
		@RequestPart("uploadExcelFile") byte[] uploadExcelFile){
		Map<String, String> msg = excelImporter.persist(uploadExcelFile);
		ConditionConfig<Member> cc = memberCrudService.genCondtitionsAfterExecuteQueryPageable();
		cc.getMsgs().clear();
		cc.getMsgs().putAll(msg);
		String result = memberIgnoreDetail(cc);
		return result;
	}
	
	
	@RequestMapping(value="/copyCondition", method=RequestMethod.POST, produces={"application/xml", "application/json"})
	public @ResponseBody Map<String, String> copyCondition(@RequestBody ConditionConfig<Member> conditionConfig){
		memberCrudService.copyConditionConfig(conditionConfig);
		return Collections.emptyMap();
	}
	
	@RequestMapping(value="/downloadExcel", method={RequestMethod.POST, RequestMethod.GET})
	public void downloadExcel(HttpServletResponse response){
		File tempFile = excelExporter.execute(memberCrudService);
		
		try(FileInputStream fis = new FileInputStream(tempFile);){
			writeExcelToResponse(response, fis, "member.xlsx");
		}catch(Throwable t){
			throw new RuntimeException(t);
		}finally{
			try{
				FileUtils.forceDelete(tempFile);
			}catch(Throwable t){
				throw new RuntimeException(t);
			}
		}
	}
	
	private void writeExcelToResponse(HttpServletResponse response, FileInputStream fis, String fileName) throws Throwable{
		response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		response.setHeader("Pragma", "");
		response.setHeader("cache-control", "");
		response.setHeader("Content-Disposition", "attachment; filename="+fileName);
		
		ServletOutputStream sos = response.getOutputStream();
		IOUtils.copy(fis, sos);
		sos.close();
	}
	
	@RequestMapping(value="/updateMemberDiscount", method=RequestMethod.POST)
	public @ResponseBody Member updateMemberDiscount(@RequestBody Member member){
		discount.applyRule(member);
		useStatus.applyRule(member);
		return member;
	}
	
	@RequestMapping(value="/downloadTemplate", method={RequestMethod.GET, RequestMethod.POST})
	public void downloadTemplate(HttpServletResponse response){
		String root = WebUtils.currentServletContext().getRealPath("/");
		String filePath = root + "\\member_sample.xlsx";
		try(FileInputStream fis = new FileInputStream(filePath);){
			writeExcelToResponse(response, fis, "member_template.xlsx");
		}catch(Throwable t){
			throw new RuntimeException(t);
		}
	}
	
}
