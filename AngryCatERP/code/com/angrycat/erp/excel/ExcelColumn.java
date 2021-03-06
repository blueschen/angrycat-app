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
		public static final int 手機電話			= index++;
		public static final int 室內電話			= index++;
		public static final int 郵遞區號			= index++;
		public static final int 地址				= index++;		
		public static final int 備註				= index++;
		public static final int 國家代碼			= index++;
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
	
	public static class OnePosClient{
		static int idx = 0;
		public static final int 客戶編號		= idx++;
		public static final int 客戶名稱		= idx++;
		public static final int 類別編號		= idx++;
		public static final int 聯絡人		= idx++;
		public static final int 公司名稱		= idx++;
		public static final int 地址第1行		= idx++;
		public static final int 地址第2行		= idx++;
		public static final int 地址第3行		= idx++;
		public static final int 電話1		= idx++;
		public static final int 電話2		= idx++;
		public static final int 傳真			= idx++;
		public static final int 電郵			= idx++;
		public static final int 級別			= idx++;
		public static final int 彈出提示		= idx++;
		public static final int 生日日期		= idx++;
		public static final int 新增日期		= idx++;
		public static final int 網址			= idx++;
		public static final int 預設折扣		= idx++;
		public static final int COLUMN_COUNT = idx;
	}
	
	public static class SalesDetail{
		public static class Fb{
			static int idx = 0;
			public static final int 狀態		= idx++;
			public static final int FB名稱	= idx++;
			public static final int 活動		= idx++;
			public static final int 型號		= idx++;
			public static final int 產品名稱	= idx++;
			public static final int 含運價格	= idx++;
			public static final int 會員價	= idx++;
			public static final int 順序		= idx++;
			public static final int 接單日	= idx++;
			public static final int 其他備註	= idx++;
			public static final int 對帳狀態	= idx++;
			public static final int 身分證字號= idx++;
			public static final int 折扣類型	= idx++;
			public static final int 是否已到貨= idx++;
			public static final int 出貨日	= idx++;
			public static final int 郵寄方式	= idx++;
			public static final int 備註		= idx++;
			public static final int COLUMN_COUNT = idx;
		}
		public static class EsliteDunnan{
			static int idx = 0;
			public static final int 狀態		= idx++;
			public static final int FB名稱	= idx++;
			public static final int 銷售日期	= idx++;
			public static final int 型號		= idx++;
			public static final int 產品名稱	= idx++;
			public static final int 定價		= idx++;
			public static final int 會員價	= idx++;
			public static final int 付款日期	= idx++;
			public static final int 身分證字號= idx++;
			public static final int 折扣類型	= idx++;
			public static final int 備註		= idx++;
			public static final int 出貨日	= idx++;
			public static final int 聯絡方式	= idx++;
			public static final int 登單者	= idx++;
			public static final int COLUMN_COUNT = idx;
		}
	}
	
	public static void main(String[]args){
		System.out.println(Product.Sheet2.定價);
	}
}
