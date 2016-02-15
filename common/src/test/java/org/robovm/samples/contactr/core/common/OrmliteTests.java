package org.robovm.samples.contactr.core.common;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableUtils;

import org.junit.Before;
import org.junit.Test;
import org.robovm.samples.contractr.core.common.OrmliteOpenHelper;
import org.robovm.samples.contractr.core.common.RoboVMContext;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by kgalligan on 2/14/16.
 */
public class OrmliteTests
{

    private TestRoboVMContext testRoboVMContext;

    @Before
    public void setup() throws Exception
    {
        Class.forName("org.sqlite.JDBC");

        testRoboVMContext = new TestRoboVMContext(false, new File("."));

    }

    @Test
    public void testHelper() throws SQLException
    {
        TestOrmliteOpenHelper openHelper = new TestOrmliteOpenHelper(testRoboVMContext, "asdf",
                                                                     BasicFields.class);
        BasicFields basicFields = new BasicFields();
        basicFields.name = "qwert";
        Dao<BasicFields, Long> dao = openHelper.getDao(BasicFields.class);
        dao.create(basicFields);
        BasicFields fromDb = dao.queryForId(basicFields.id);
        System.out.println(fromDb.name);
    }

    @DatabaseTable static class BasicFields
    {
        @DatabaseField(generatedId = true)
        public long id;

        @DatabaseField
        public String name;


    }

    class TestOrmliteOpenHelper extends OrmliteOpenHelper
    {

        private Class[] cl;

        public TestOrmliteOpenHelper(RoboVMContext context, String databaseName, Class... cl)
        {
            super(context, databaseName, 1);
            this.cl = cl;
        }

        @Override
        public void onCreate(Connection db, ConnectionSource connectionSource)
        {
            try
            {
                for(Class clz : cl)
                {
                    TableUtils.createTableIfNotExists(connectionSource, clz);
                }
            }
            catch(SQLException e)
            {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onUpgrade(Connection db, ConnectionSource connectionSource, int oldVersion, int newVersion)
        {
            try
            {
                for(int i = cl.length - 1; i >= 0; i--)
                {
                    Class clz = cl[i];
                    TableUtils.dropTable(connectionSource, clz, true);
                }
            }
            catch(SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
}
