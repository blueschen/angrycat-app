package com.angrycat.erp.onepos.mdb;

import java.io.File;
import java.io.IOException;

import net.ucanaccess.jdbc.JackcessOpenerInterface;

import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.DatabaseBuilder;

public class CryptCodecOpener implements JackcessOpenerInterface {

	@Override
	public Database open(File file, String pwd) throws IOException {
		DatabaseBuilder dbd = new DatabaseBuilder(file);
		dbd.setAutoSync(false);
		dbd.setCodecProvider(new CryptCodecProvider(pwd));
		dbd.setReadOnly(false);
		return dbd.open();
	}

}
