package com.angrycat.erp.onepos.mdb;

import java.io.File;
import java.util.function.Consumer;

import org.springframework.stereotype.Service;

import com.angrycat.erp.function.FunctionThrowable;
import com.angrycat.erp.onepos.vo.INV_Headers;
import com.angrycat.erp.onepos.vo.INV_Items;
import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

@Service
public class OnePosMdbAccessor {
	
	private String path = "C:\\ONE-POS DB\\onepos.dat";
	private String pwd = "31072100";
	
	public void openOnePos(Consumer<Database> execution){
		openOnePos((db)->{
			execution.accept(db);
			return null;
		});
	}
	public <T>T openOnePos(FunctionThrowable<Database, T> execution){
		T results = null;
		try(Database db = new DatabaseBuilder(new File(path))
		.setCodecProvider(new CryptCodecProvider(pwd))
		.open();){			
			results = execution.apply(db);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return results;
	}
	
	private static void testOpenOnePos(){
		OnePosMdbAccessor accessor = new OnePosMdbAccessor();
		accessor.openOnePos(db->{
			db.getTable("INV_Headers");
			
			return null;
		});
	}
	
	private void processInvHeaders(){
		openOnePos(db->{
			try{
				Table table = db.getTable("INV_Headers");
				INV_Headers.keyPrintf(table);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		});
	}
	private void processInvItems(){
		openOnePos(db->{
			try{
				Table table = db.getTable("INV_Items");
				INV_Items.keyPrintf(table);
//				table.forEach(row->{
//					INV_Items.toVo(row);
//				});
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		});		
	}
	public static void testProcessInvItems(){
		OnePosMdbAccessor accessor = new OnePosMdbAccessor();
		accessor.processInvItems();
	}
	public static void testProcessInvHeaders(){
		OnePosMdbAccessor accessor = new OnePosMdbAccessor();
		accessor.processInvHeaders();
	}
	public static void main(String[]args){
		testProcessInvItems();
	}
}
