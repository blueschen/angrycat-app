package com.angrycat.erp.onepos.mdb;

import java.beans.PropertyDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;

import javax.persistence.Column;

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

	public void openOnePos(Consumer<Database> execution) {
		openOnePos((db) -> {
			execution.accept(db);
			return null;
		});
	}

	public <T> T openOnePos(FunctionThrowable<Database, T> execution) {
		T results = null;
		try (Database db = new DatabaseBuilder(new File(path))
				.setCodecProvider(new CryptCodecProvider(pwd)).open();) {
			results = execution.apply(db);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return results;
	}

	/**
	 * 確保net.ucanaccess.jdbc.UcanaccessDriver是在第一順位
	 * 因為先註冊org.mariadb.jdbc.Driver，
	 * 導致不管怎麼拿都沒辦法使用net.ucanaccess.jdbc.UcanaccessDriver
	 * 做法是，任何在net.ucanaccess.jdbc.UcanaccessDriver之前的Driver都會先被移除再註冊 ref.
	 * http://stackoverflow.com/questions/31304195/wrong-jdbc-driver-being-used
	 * TODO 在Web環境下如果要同時使用兩者，需要測試有無狀況
	 */
	private static void moveUcanaccessDriverToFirst() {
		// ref. http://ucanaccess.sourceforge.net/site.html
		Driver driver = null;
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			driver = drivers.nextElement();
			System.out.println(driver.getClass().getName());
			if (!driver.getClass().getName()
					.equals("net.ucanaccess.jdbc.UcanaccessDriver")) {
				try {
					DriverManager.deregisterDriver(driver);
					DriverManager.registerDriver(driver);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			} else {
				break;
			}
		}
	}

	/**
	 * 透過ucanaccess函式庫，可以以jdbc和sql語法查詢Microsoft Access取得資料。並以適合的POJO傳回查詢結果
	 * mapping class要用JPA annotation定義好對應欄位名稱
	 * 
	 * @param mdb
	 * @param sql
	 * @param params
	 * @param mappingClz
	 * @return
	 */
	public static <T> List<T> selectMdb(String mdb, String sql,
			List<Object> params, Class<T> mappingClz) {
		int paramsCount = StringUtils.countMatches(sql, "?");
		if (paramsCount != params.size()) {
			throw new RuntimeException("selectMdb 參數數量沒有正確對應!!");
		}

		List<String> columnNames = new ArrayList<>();
		List<Method> writeMethods = new ArrayList<>();
		// getColNamesAndWriteMethods在此處呼叫，是希望能夠盡早檢核mapping class是否有定義column
		// name
		getColNamesAndWriteMethods(mappingClz, columnNames, writeMethods);

		moveUcanaccessDriverToFirst();

		String user = "sa";
		String encryptPwd = "31072100";
		String jackcessOpener = CryptCodecOpener.class.getName();
		String jdbcUrl = "jdbc:ucanaccess://" + mdb + ";jackcessOpener="
				+ jackcessOpener;

		List<T> pojos = Collections.emptyList();
		try (Connection conn = DriverManager.getConnection(jdbcUrl, user,
				encryptPwd);
				PreparedStatement prepared = conn.prepareStatement(sql);) {
			for (int i = 0; i < params.size(); i++) {
				Object param = params.get(i);
				prepared.setObject(i + 1, param);
			}
			try (ResultSet rs = prepared.executeQuery();) {
				pojos = collectPojosFromResultSet(rs, mappingClz, columnNames,
						writeMethods);
				pojos.stream()
						.forEach(
								p -> {
									System.out.println(ReflectionToStringBuilder
											.toString(
													p,
													ToStringStyle.MULTI_LINE_STYLE));
								});
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		System.out.println("共" + pojos.size() + "筆");
		return pojos;
	}

	public static void testUcanaccess() {
		// ref. http://ucanaccess.sourceforge.net/site.html
		moveUcanaccessDriverToFirst();

		String mdb = "E:\\angrycat_workitem\\pos\\onepos\\2016_05_19\\onepos.mdb";
		String user = "sa";
		String encryptPwd = "31072100";
		String jackcessOpener = CryptCodecOpener.class.getName();
		String jdbcUrl = "jdbc:ucanaccess://" + mdb + ";jackcessOpener="
				+ jackcessOpener;

		List<String> ids = Arrays.asList("AAH029", "AAL025");
		List<String> params = Collections.nCopies(ids.size(), "?");
		String paramsStr = StringUtils.join(params, ",");
		String sql = "SELECT * FROM Products WHERE productid IN (" + paramsStr
				+ ")";

		try (Connection conn = DriverManager.getConnection(jdbcUrl, user,
				encryptPwd);
				PreparedStatement prepared = conn.prepareStatement(sql);) {

			for (int i = 0; i < ids.size(); i++) {
				String id = ids.get(i);
				prepared.setString(i + 1, id);
			}
			try (ResultSet rs = prepared.executeQuery();) {
				// int columnCount = meta.getColumnCount();
				// for(int i = 0; i < columnCount; i++){
				// String label = meta.getColumnLabel(i); // not work via
				// net.ucanaccess.jdbc.UcanaccessDriver
				// String columnName = meta.getColumnName(i); // not work via
				// net.ucanaccess.jdbc.UcanaccessDriver
				// System.out.println(label + ":" + columnName);
				// }

				List<OnePosProduct> products = collectPojosFromResultSet(rs,
						OnePosProduct.class);
				products.stream()
						.forEach(
								p -> {
									System.out.println(ReflectionToStringBuilder
											.toString(
													p,
													ToStringStyle.MULTI_LINE_STYLE));
								});
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * see the method
	 * {@link #collectPojosFromResultSet(ResultSet, Class, List, List)}.
	 * 
	 * @param rs
	 * @param clz
	 * @return
	 */
	private static <T> List<T> collectPojosFromResultSet(ResultSet rs,
			Class<T> clz) {
		List<String> columnNames = new ArrayList<>();
		List<Method> writeMethods = new ArrayList<>();

		getColNamesAndWriteMethods(clz, columnNames, writeMethods);
		List<T> targets = collectPojosFromResultSet(rs, clz, columnNames,
				writeMethods);
		return targets;
	}

	/**
	 * 使用JPA的annotation定義mapping class的column name，再透過反射取得及設定值 column name定義範例:
	 * @Column(name="productid", columnDefinition="產品編號") mapping
	 * class命名必須嚴格遵循JavaBean命名慣例，譬如boolean的getter為isXxx，Boolean是getXxx mapping
	 * class必須提供一個無參數的建構子
	 * 
	 * @param rs
	 * @param clz
	 * @param columnNames
	 * @param writeMethods
	 * @return
	 */
	private static <T> List<T> collectPojosFromResultSet(ResultSet rs,
			Class<T> clz, List<String> columnNames, List<Method> writeMethods) {
		List<T> targets = new ArrayList<>();
		try {
			while (rs.next()) {
				T target = clz.newInstance();
				for (int i = 0; i < columnNames.size(); i++) {
					String columnName = columnNames.get(i);
					Method writeMethod = writeMethods.get(i);
					Object val = rs.getObject(columnName);
					if (val != null) {
						// System.out.println("columnName: " + columnName +
						// ", value class: " + val.getClass().getName() +
						// ", val: " + val);
						writeMethod.invoke(target, val);
					}
				}
				targets.add(target);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return targets;
	}

	/**
	 * 取得欄位名稱及setter，若傳入mapping class沒有定義任何一個欄位名稱，就會丟出例外
	 * 
	 * @param clz
	 * @param columnNames
	 * @param writeMethods
	 */
	private static <T> void getColNamesAndWriteMethods(Class<T> clz,
			List<String> columnNames, List<Method> writeMethods) {
		PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(clz);
		for (PropertyDescriptor p : ps) {
			Method writeMethod = p.getWriteMethod();
			Method readMethod = p.getReadMethod();

			if (readMethod != null) {
				Column[] columns = readMethod
						.getAnnotationsByType(Column.class);
				if (columns.length != 0) {
					String columnName = columns[0].name();
					columnNames.add(columnName);
					writeMethods.add(writeMethod);
				}
			} else {
				System.out.println(p.getName() + " readMethod is NULL");
			}
		}
		if (columnNames.size() == 0) {
			throw new RuntimeException(
					"collectPojosFromResultSet mapping class沒有在getter上用JPA定義column name");
		}
	}

	private static void testOpenOnePos() {
		OnePosMdbAccessor accessor = new OnePosMdbAccessor();
		accessor.openOnePos(db -> {
			db.getTable("INV_Headers");

			return null;
		});
	}

	private void processInvHeaders() {
		openOnePos(db -> {
			try {
				Table table = db.getTable("INV_Headers");
				INV_Headers.keyPrintf(table);
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
	}

	private void processInvItems() {
		openOnePos(db -> {
			try {
				Table table = db.getTable("INV_Items");
				INV_Items.keyPrintf(table);
				// table.forEach(row->{
				// INV_Items.toVo(row);
				// });
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static void testProcessInvItems() {
		OnePosMdbAccessor accessor = new OnePosMdbAccessor();
		accessor.processInvItems();
	}

	public static void testProcessInvHeaders() {
		OnePosMdbAccessor accessor = new OnePosMdbAccessor();
		accessor.processInvHeaders();
	}

	public static void main(String[] args) {
		testUcanaccess();
	}
}
