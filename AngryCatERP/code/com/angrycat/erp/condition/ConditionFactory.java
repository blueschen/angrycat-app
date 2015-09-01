package com.angrycat.erp.condition;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 
 * @author JERRY LIN
 *
 */
public class ConditionFactory implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6645678494885515364L;
	
	/**
	 * put conditions into a group, ex. (p.id = :pId AND p.name = :pName AND p.code = :pCode)
	 * @return
	 */
	public static Conjunction conjunction(){
		return new Conjunction();
	}
	/**
	 * put conditions into a group, ex. (p.id = :pId OR p.name = :pName OR p.code = :pCode)
	 * @return
	 */
	public static Disjunction disjunction(){
		return new Disjunction();
	}
	private static SimpleExpression newSimpleInstance(String propertyName, String operator, String id){
		SimpleExpression expression = new SimpleExpression();
		expression.setId(id);
		expression.setPropertyName(propertyName);
		expression.setOperator(operator);
		return expression;
	}
	private static String findFirstMatch(String regex, String input){
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(input);
		String found = "";
		while(m.find()){
			int start = m.start();
			int end = m.end();
			found = input.substring(start, end);
		}
		return found;
	}
	private static String findPropertyName(String input){
		String regex = "[a-zA-Z0-9\\.]+\\.[a-zA-Z0-9\\.]+";
		String found = findFirstMatch(regex, input);
		return found;
	}
	private static String findOperator(String input){
		String regex = "(\\s+(IN|in|LIKE|like|NOT\\s+LIKE)\\s+)|\\s*(\\>\\=|\\<\\=|\\!\\=|\\=|\\<\\>|\\>|\\<)\\s*";
		String found = findFirstMatch(regex, input);
		found = found.trim();
		return found;
	}
	private static String findNamedParam(String input){
		String regex = "(\\:|\\(\\:){1}\\w+\\)?";
		String found = findFirstMatch(regex, input);
		regex = "([^\\:]|[^(\\(\\:)]){1}\\w+[^\\)]?";
		found = findFirstMatch(regex, found);
		return found;
	}
	private static SimpleExpression findSimpleExpression(String expr){
		String propertyName = findPropertyName(expr);
		System.out.println("propertyName: " + propertyName);
		String operator = findOperator(expr);
		String namedParam = findNamedParam(expr);
		SimpleExpression expression = newSimpleInstance(propertyName, operator, namedParam);
		return expression;
	}
	public static SimpleExpression putStr(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(String.class);
		return expression;
	}
	public static SimpleExpression putStr(String expr, String val){
		SimpleExpression expression = putStr(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putInt(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(Integer.class);
		return expression;
	}
	public static SimpleExpression putInt(String expr, Integer val){
		SimpleExpression expression = putStr(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putDouble(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(Double.class);
		return expression;
	}
	public static SimpleExpression putDouble(String expr, Double val){
		SimpleExpression expression = putStr(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putFloat(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(Float.class);
		return expression;
	}
	public static SimpleExpression putFloat(String expr, Float val){
		SimpleExpression expression = putStr(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putSqlDate(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(Date.class);
		return expression;
	}
	public static SimpleExpression putSqlDate(String expr, Date val){
		SimpleExpression expression = putStr(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putTimestamp(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(Timestamp.class);
		return expression;
	}
	public static SimpleExpression putTimestamp(String expr, Timestamp val){
		SimpleExpression expression = putTimestamp(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putTimestampStart(String expr){
		String propertyName = findPropertyName(expr);
		String operator = findOperator(expr);
		String namedParam = findNamedParam(expr);
		SimpleExpression expression = new TimestampStartExpression();
		expression.setId(namedParam);
		expression.setOperator(operator);
		expression.setPropertyName(propertyName);
		expression.setType(Timestamp.class);
		return expression;
	}
	public static SimpleExpression putTimestampStart(String expr, Timestamp val){
		SimpleExpression expression = putTimestampStart(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putTimestampEnd(String expr){
		String propertyName = findPropertyName(expr);
		String operator = findOperator(expr);
		String namedParam = findNamedParam(expr);
		SimpleExpression expression = new TimestampEndExpression();
		expression.setId(namedParam);
		expression.setOperator(operator);
		expression.setPropertyName(propertyName);
		expression.setType(Timestamp.class);
		return expression;
	}
	public static SimpleExpression putTimestampEnd(String expr, Timestamp val){
		SimpleExpression expression = putTimestampEnd(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putBoolean(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(Boolean.class);
		return expression;
	}
	public static SimpleExpression putBoolean(String expr, Boolean val){
		SimpleExpression expression = putBoolean(expr);
		expression.setValue(val);
		return expression;
	}
	public static SimpleExpression putInList(String expr){
		SimpleExpression expression = findSimpleExpression(expr);
		expression.setType(List.class);
		return expression;
	}
	public static SimpleExpression putInList(String expr, List<?> val){
		SimpleExpression expression = putStr(expr);
		expression.setValue(val);
		return expression;
	}
	private static LikeExpression newLikeExpression(String propertyName, String operator, String id, MatchMode matchMode){
		LikeExpression like = new LikeExpression();
		like.setId(id);
		like.setPropertyName(propertyName);
		like.setOperator(operator);
		like.setMatchMode(matchMode);
		like.setType(String.class);
		return like;
	}
	public static LikeExpression putStr(String expr, MatchMode matchMode){
		String operator = findOperator(expr);
		String oper = operator.toLowerCase();
		if(!"like".equals(oper)
		&&!"not like".equals(oper)){
			throw new RuntimeException("like operator NOT existed!!");
		}
		String propertyName = findPropertyName(expr);
		String namedParam = findNamedParam(expr);
		
		LikeExpression like = newLikeExpression(propertyName, operator, namedParam, matchMode);
		return like;
	}
	public static LikeExpression putStrCaseInsensitive(String expr, MatchMode matchMode){
		LikeExpression like = putStr(expr, matchMode);
		like.setCaseInsensitive(true);
		return like;
	}
	public static LikeExpression putStr(String expr, MatchMode matchMode, String val){
		LikeExpression like = putStr(expr, matchMode);
		like.setValue(val);
		return like;
	}
	public static LikeExpression putStrCaseInsensitive(String expr, MatchMode matchMode, String val){
		LikeExpression like = putStrCaseInsensitive(expr, matchMode);
		like.setValue(val);
		return like;
	}
	public static LikeExpression exactMatchLike(String expr){
		LikeExpression like = putStr(expr, MatchMode.EXACT);
		return like;
	}
	public static LikeExpression exactMatchLike(String expr, String val){
		LikeExpression like = exactMatchLike(expr);
		like.setValue(val);
		return like;
	}
	public static LikeExpression anyWhereMatchLike(String expr){
		LikeExpression like = putStr(expr, MatchMode.ANYWHERE);
		return like;
	}
	public static LikeExpression anyWhereMatchLike(String expr, String val){
		LikeExpression like = anyWhereMatchLike(expr);
		like.setValue(val);
		return like;
	}
	public static LikeExpression startMatchLike(String expr){
		LikeExpression like = putStr(expr, MatchMode.START);
		return like;
	}
	public static LikeExpression startMatchLike(String expr, String val){
		LikeExpression like = startMatchLike(expr);
		like.setValue(val);
		return like;
	}
	public static LikeExpression endMatchLike(String expr){
		LikeExpression like = putStr(expr, MatchMode.END);
		return like;
	}
	public static LikeExpression endMatchLike(String expr, String val){
		LikeExpression like = endMatchLike(expr);
		like.setValue(val);
		return like;
	}
	/**
	 * without parameter value, only through hql(or sql, or jpql) operator to filter, ex. p.name IS NOT NULL
	 * @param propertyDesc
	 * @return
	 */
	public static PropertyDescExpression propertyDesc(String propertyDesc){
		PropertyDescExpression expression = new PropertyDescExpression();
		expression.setPropertyDesc(propertyDesc);
		return expression;
	}

}
