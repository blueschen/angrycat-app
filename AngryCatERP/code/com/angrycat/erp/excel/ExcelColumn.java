package com.angrycat.erp.excel;


public class ExcelColumn {
	public static class Member{
		static int index = 0;
		
		public static final int 索引				= index++;
		public static final int 轉VIP日期			= index++;
		public static final int Ohmliy_VIP		= index++;
		public static final int 生日使用8折優惠	= index++;
		public static final int Facebook_姓名	= index++;
		public static final int 真實姓名			= index++;
		public static final int 性別				= index++;
		public static final int 身份證字號		= index++;
		public static final int 出生年月日		= index++;
		public static final int 電子信箱			= index++;
		public static final int 聯絡電話			= index++;
		public static final int 郵遞區號			= index++;
		public static final int 地址				= index++;		
		public static final int 備註				= index++;
		public static final int VIP延續			= index++;
		
		public static final int COLUMN_COUNT	= index;
		
	}
	
	public static class Product{
		public static class Sheet1{
			static int idx = 0;
			public static final int 類別		= idx++;
			public static final int 型號		= idx++;
			public static final int 英文名字	= idx++;
			public static final int 定價		= idx++;
			public static final int SHEET1_COLUMN_COUNT = idx;
		}
		public static class Sheet2{
			static int idx = 0;
			public static final int 類別		= idx++;
			public static final int 型號		= idx++;
			public static final int 英文名字	= idx++;
			public static final int 定價		= idx++;
			public static final int 系列名	= idx++;
			public static final int 圖片		= idx++;
			public static final int SHEET1_COLUMN_COUNT = idx;
		}
		public static class Sheet3{
			static int idx = 0;
			public static final int 類別		= idx++;
			public static final int 型號		= idx++;
			public static final int 英文名字	= idx++;
			public static final int 定價		= idx++;
			public static final int 圖片		= idx++;
			public static final int SHEET1_COLUMN_COUNT = idx;
		}
		public static class OnePos{
			static int idx = 0;
			public static final int 產品編號		= idx++;
			public static final int 產品名稱		= idx++;
			public static final int 類別編號		= idx++;
			public static final int 性質			= idx++;
			public static final int 條碼編號		= idx++;
			public static final int 售價			= idx++;
			public static final int VIP售價		= idx++;
			public static final int 分級售價_3	= idx++;
			public static final int 分級售價_4	= idx++;
			public static final int 分級售價_5	= idx++;
			public static final int 單位成本		= idx++;
			public static final int 品牌編號		= idx++;
			public static final int 型號			= idx++;
			public static final int 備註欄		= idx++;
			public static final int 供應商編號	= idx++;
			public static final int 單位			= idx++;
			public static final int 庫存			= idx++;
			public static final int SHEET1_COLUMN_COUNT = idx;
		}
	}
}
