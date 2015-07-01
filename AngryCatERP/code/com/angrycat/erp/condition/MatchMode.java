package com.angrycat.erp.condition;

/**
 * @author JERRY LIN
 *
 */
public enum MatchMode {

	
	ANYWHERE {
		@Override
		public String transformer(String input) {
			return "%"+input+"%";
		}
	},
	END {
		@Override
		public String transformer(String input) {
			return "%"+input;
		}
	},
	EXACT {
		@Override
		public String transformer(String input) {
			return input;
		}
	},
	START {
		@Override
		public String transformer(String input) {
			return input+"%";
		}
	};			
	public abstract String transformer(String input);
}
