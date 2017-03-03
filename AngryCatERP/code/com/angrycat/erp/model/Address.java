package com.angrycat.erp.model;

public class Address {
	private String id;
	private String city; // 縣市:city市|county縣
	private String area; // 鄉鎮市區:township鄉鎮|city市|district區 
	private String road; // 路(街)名或鄉里名稱:village村(里)|road路|street街|boulevard大道
	private int neighborhood; // 鄰
	private int lane; // 巷
	private int alley; // 弄
	private int subAlley; // 衖
	private int number; // 號
	private int subNumber; // 之
	private int floor; // 樓
	private int subFloor; // 之
	private int room; // 室
	private int postCode;
}
