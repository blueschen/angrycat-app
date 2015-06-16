package com.angrycat.erp.condition;

import java.util.function.Function;
/**
 * @author JERRY LIN
 *
 */
public enum MatchMode {

	
	ANYWHERE {
		@Override
		public Function<SimpleExpression, Object> transformer() {
			return ANYWHERE_FORMATTER;
		}
	},
	END {
		@Override
		public Function<SimpleExpression, Object> transformer() {
			return END_FORMATTER;
		}
	},
	EXACT {
		@Override
		public Function<SimpleExpression, Object> transformer() {
			return EXACT_FORMATTER;
		}
	},
	START {
		@Override
		public Function<SimpleExpression, Object> transformer() {
			return START_FORMATTER;
		}
	};
	private static final Function<SimpleExpression, Object> ANYWHERE_FORMATTER = 
		new Function<SimpleExpression, Object>(){
			@Override
			public Object apply(SimpleExpression t){
				return "%"+t.getValue().toString()+"%";
			}};
	private static final Function<SimpleExpression, Object> END_FORMATTER = 
		new Function<SimpleExpression, Object>(){
			@Override
			public Object apply(SimpleExpression t){
				return "%"+t.getValue().toString();
			}};
	private static final Function<SimpleExpression, Object> START_FORMATTER = 
		new Function<SimpleExpression, Object>(){
			@Override
			public Object apply(SimpleExpression t){
				return t.getValue().toString()+"%";
			}};
	private static final Function<SimpleExpression, Object> EXACT_FORMATTER = 
		new Function<SimpleExpression, Object>(){
			@Override
			public Object apply(SimpleExpression t){
				return t.getValue();
			}};				
	public abstract Function<SimpleExpression, Object> transformer();
}
