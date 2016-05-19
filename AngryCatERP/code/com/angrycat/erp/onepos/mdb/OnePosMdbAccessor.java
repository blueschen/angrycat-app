package com.angrycat.erp.onepos.mdb;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.Column;

import net.ucanaccess.jdbc.UcanaccessConnection;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.angrycat.erp.function.FunctionThrowable;
import com.angrycat.erp.model.OnePosProduct;
import com.angrycat.erp.onepos.vo.INV_Headers;
import com.angrycat.erp.onepos.vo.INV_Items;
import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Table;

@Service
@Scope("prototype")
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
	public static void testUcanaccess(){
		String mdb = "E:\\angrycat_workitem\\pos\\onepos\\2016_05_19\\onepos.mdb";
		String user = "sa";
		String encryptPwd = "31072100";
		String jackcessOpener = CryptCodecOpener.class.getName();
		String jdbcUrl = "jdbc:ucanaccess://"+mdb+";jackcessOpener="+jackcessOpener;
		
		Connection conn = null;
		PreparedStatement prepared = null;
		ResultSet rs = null;
		try {
			// ref. http://ucanaccess.sourceforge.net/site.html
			Driver driver = null;
			Enumeration<Driver> drivers = DriverManager.getDrivers();
			while(drivers.hasMoreElements()){
				driver = drivers.nextElement();
				System.out.println(driver.getClass().getName());
				// 因為先註冊org.mariadb.jdbc.Driver，導致不管怎麼拿都沒辦法使用net.ucanaccess.jdbc.UcanaccessDriver
				// 下面的做法是，先移除org.mariadb.jdbc.Driver，再加到最後面，這樣就會確保拿到net.ucanaccess.jdbc.UcanaccessDriver
				// TODO 在Web環境下如果要同時使用兩者，需要測試有無狀況
				// ref. http://stackoverflow.com/questions/31304195/wrong-jdbc-driver-being-used
				if(driver.getClass().getName().equals("org.mariadb.jdbc.Driver")){
					try{
						DriverManager.deregisterDriver(driver);
						DriverManager.registerDriver(driver);
					}catch(Throwable e){
						throw new RuntimeException(e);
					}
				}
			}

			conn = DriverManager.getConnection(jdbcUrl, user, encryptPwd);
			
			
			
			
			List<String> ids = Arrays.asList("AAH029"
					,"AAL025"
					,"AAN003"
					,"AAP006"
					,"AAU001"
					,"AAU002"
					,"AAU003"
					,"AAU004"
					,"AAX100"
					,"AAX101"
					,"AAX102"
					,"AAX103"
					,"AAX104"
					,"AAX105"
					,"AAY026"
					,"ACS000"
					,"ACS000G"
					,"ACS001"
					,"ACS002"
					,"ACS002G"
					,"AMV01900"
					,"WHG001"
					,"WHG002"
					,"WHG003"
					,"WHG004"
					,"WHG007"
					,"WHG010"
					,"WHG012"
					,"WHG015"
					,"WHG016"
					,"WHG017"
					,"WHG018"
					,"WHG019"
					,"WHG020"
					,"WHG021"
					,"WHG024"
					,"WHG026"
					,"WHH001"
					,"WHH002"
					,"WHH003"
					,"WHH004"
					,"WHH005"
					,"WHH009"
					,"WHH010"
					,"AAL024"
					,"AAL031"
					,"AHKS002"
					,"AHKS003"
					,"AHKS004"
					,"AMV02000"
					,"AAA056"
					,"AAA057"
					,"AAA058"
					,"AAA059"
					,"AAA060"
					,"AAA061"
					,"BGL004BXS"
					,"BGL004BS"
					,"BGL004BM"
					,"BGL004BL"
					,"WHW01950"
					,"WHW01970"
					,"WHW01950B"
					,"WHW01970B");
			
			List<String> params = Collections.nCopies(ids.size(), "?");
			String paramsStr = StringUtils.join(params, ",");
			
			prepared = conn.prepareStatement("SELECT * FROM Products WHERE productid IN ("+paramsStr+")");
			for(int i = 0; i < ids.size(); i++){
				String id = ids.get(i);
				prepared.setString(i+1, id);
			}
			rs = prepared.executeQuery();
			ResultSetMetaData meta= rs.getMetaData();
//			int columnCount = meta.getColumnCount();
//			for(int i = 0; i < columnCount; i++){
//				String label = meta.getColumnLabel(i); // not work via net.ucanaccess.jdbc.UcanaccessDriver
//				String columnName = meta.getColumnName(i); // not work via net.ucanaccess.jdbc.UcanaccessDriver
//				System.out.println(label + ":" + columnName);
//			}
			
			List<OnePosProduct> products = collectPojosFromResultSet(rs, OnePosProduct.class);
			products.stream().forEach(p->{
				System.out.println(ReflectionToStringBuilder.toString(p, ToStringStyle.MULTI_LINE_STYLE));
			});
			
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			try{
				if(rs!=null){
					rs.close();
				}
				if(prepared!=null){
					prepared.close();
				}
				if(conn != null){
					conn.close();
				}
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		}
	}
	
	/**
	 * 使用JPA的annotation定義mapping class的column name，再透過反射取得及設定值
	 * column name定義範例: @Column(name="productid", columnDefinition="產品編號")
	 * mapping class命名必須嚴格遵循JavaBean命名慣例，譬如boolean的getter為isXxx，Boolean是getXxx
	 * mapping class必須提供一個無參數的建構子
	 * @param rs
	 * @param clz
	 * @return
	 */
	private static <T>List<T> collectPojosFromResultSet(ResultSet rs, Class<T> clz){
		List<T> targets = new ArrayList<>();
		try {
			PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(clz);
			List<String> columnNames = new ArrayList<>();
			List<Method> writeMethods = new ArrayList<>();
			for(PropertyDescriptor p : ps){
				Method writeMethod = p.getWriteMethod();
				Method readMethod = p.getReadMethod();
				
				if(readMethod != null){
					Column[] columns = readMethod.getAnnotationsByType(Column.class);
					if(columns.length != 0){
						String columnName = columns[0].name();
						columnNames.add(columnName);
						writeMethods.add(writeMethod);
					}
				}else{
					System.out.println(p.getName() + " readMethod is NULL");
				}
			}
			while(rs.next()){
				T target = clz.newInstance();
				for(int i = 0; i < columnNames.size(); i++){
					String columnName = columnNames.get(i);
					Method writeMethod = writeMethods.get(i);
					Object val = rs.getObject(columnName);
					if(val != null){
//						System.out.println("columnName: " + columnName + ", value class: " + val.getClass().getName() + ", val: " + val);
						writeMethod.invoke(target, val);
					}
				}
				targets.add(target);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		System.out.println("共"  + targets.size() + "筆");
		return targets;
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
		testUcanaccess();
	}
}
