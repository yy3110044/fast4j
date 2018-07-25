package com.yy.fast4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * mybatis mapper xml生成器
 * @author yy
 *
 */
public class MybatisMapperXmlGenerator {
private static final Logger logger = LogManager.getLogger(MybatisMapperXmlGenerator.class);
	
	private static final Class<?> qcClass = QueryCondition.class;

	private final Class<?> poClass;
	private final String tableName;
	private String mapperPackage;
	private String servicePackage;
	private String serviceImplPackage;
	private final String javaSourceFolder;
	
	public MybatisMapperXmlGenerator(Class<?> poClass,
                                      String tableName,
                                      String javaSourceFolder,
			                          String mapperPackage,
			                          String servicePackage,
			                          String serviceImplPackage
                                      ) {
		this.poClass = poClass;
		this.tableName = tableName;
		this.javaSourceFolder = javaSourceFolder;
		this.mapperPackage = mapperPackage;
		this.servicePackage = servicePackage;
		this.serviceImplPackage = serviceImplPackage;
	}
	
	//生成mapper接口字符串
	public String generateMapperInterfaceSourceStr() {
		StringBuilder sb = new StringBuilder();
		sb.append("package ").append(mapperPackage).append(";").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("import java.util.List;").append(System.lineSeparator());
		sb.append("import org.apache.ibatis.annotations.Mapper;").append(System.lineSeparator());
		sb.append("import ").append(poClass.getName()).append(";").append(System.lineSeparator());
		sb.append("import ").append(qcClass.getName()).append(";").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("@Mapper").append(System.lineSeparator());
		sb.append("public interface ").append(poClass.getSimpleName()).append("Mapper {").append(System.lineSeparator());
		sb.append("    void add(").append(poClass.getSimpleName()).append(" obj);").append(System.lineSeparator());
		sb.append("    void delete(").append(getPoIdTypeName()).append(" id);").append(System.lineSeparator());
		sb.append("    void update(").append(poClass.getSimpleName()).append(" obj);").append(System.lineSeparator());
		sb.append("    ").append(poClass.getSimpleName()).append(" find(").append(qcClass.getSimpleName()).append(" qc);").append(System.lineSeparator());
		sb.append("    ").append(poClass.getSimpleName()).append(" findById(").append(getPoIdTypeName()).append(" id);").append(System.lineSeparator());
		sb.append("    ").append("List<").append(poClass.getSimpleName()).append(">").append(" query(").append(qcClass.getSimpleName()).append(" qc);").append(System.lineSeparator());
		sb.append("    ").append("int getCount(").append(qcClass.getSimpleName()).append(" qc);").append(System.lineSeparator());
		sb.append("}").append(System.lineSeparator());
		return sb.toString();
	}
	
	//生成mapper接口字符串并保存到文件
	public void generateMapperInterfaceToFile() {
		this.saveStrToFile(generateMapperInterfaceSourceStr(), new File(this.getMapperFolder(), poClass.getSimpleName() + "Mapper.java"));
	}
	
	//生成mapper xml字符串
	public String generateMapperXmlStr() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>").append(System.lineSeparator());
		sb.append("<!DOCTYPE mapper").append(System.lineSeparator());
		sb.append("    PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\"").append(System.lineSeparator());
		sb.append("    \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">").append(System.lineSeparator());
		sb.append("<mapper namespace=\"").append(mapperPackage).append(".").append(poClass.getSimpleName()).append("Mapper").append("\">").append(System.lineSeparator());
		sb.append("    <insert id=\"add\" parameterType=\"").append(poClass.getName()).append("\">").append(System.lineSeparator());
		if(int.class == getPoIdType()) { //id为int类型，是自增字段
			sb.append("        <selectKey keyProperty=\"id\" order=\"AFTER\" resultType=\"int\">").append(System.lineSeparator());
			sb.append("            SELECT LAST_INSERT_ID()").append(System.lineSeparator());
			sb.append("        </selectKey>").append(System.lineSeparator());
		}
		sb.append("        ").append(this.getInsertSql()).append(System.lineSeparator());
		sb.append("    </insert>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    <delete id=\"delete\" parameterType=\"").append(getPoIdTypeName()).append("\">").append(System.lineSeparator());
		sb.append("        delete from " + this.tableName + " where id = #{id}").append(System.lineSeparator());
		sb.append("    </delete>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    <update id=\"update\" parameterType=\"").append(poClass.getName()).append("\">").append(System.lineSeparator());
		sb.append("        ").append(this.getUpdateSql()).append(System.lineSeparator());
		sb.append("    </update>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    <select id=\"find\" parameterType=\"").append(qcClass.getName()).append("\" resultType=\"").append(poClass.getName()).append("\">").append(System.lineSeparator());
		sb.append("        ").append(this.getSelectSql()).append(System.lineSeparator());
		sb.append("        <if test=\"useWhere\">").append(System.lineSeparator());
		sb.append("            where").append(System.lineSeparator());
		sb.append("            <foreach collection=\"conditions\" index=\"index\" item=\"obj\">").append(System.lineSeparator());
		sb.append("                <choose>").append(System.lineSeparator());
		sb.append("                    <when test=\"index == 0\">${obj.field} ${obj.operator} #{obj.value}</when>").append(System.lineSeparator());
		sb.append("                    <otherwise>${obj.join} ${obj.field} ${obj.operator} #{obj.value}</otherwise>").append(System.lineSeparator());
		sb.append("                </choose>").append(System.lineSeparator());
		sb.append("            </foreach>").append(System.lineSeparator());
		sb.append("        </if>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("        <if test=\"useSort\">").append(System.lineSeparator());
		sb.append("            order by").append(System.lineSeparator());
		sb.append("            <foreach collection=\"sorts\" item=\"obj\" separator=\",\">").append(System.lineSeparator());
		sb.append("                ${obj}").append(System.lineSeparator());
		sb.append("            </foreach>").append(System.lineSeparator());
		sb.append("        </if>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("        limit 0, 1").append(System.lineSeparator());
		sb.append("    </select>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    <select id=\"findById\" parameterType=\"").append(this.getPoIdTypeName()).append("\" resultType=\"").append(poClass.getName()).append("\">").append(System.lineSeparator());
		sb.append("        ").append(this.getSelectSql()).append(System.lineSeparator());
		sb.append("        where id = #{id}").append(System.lineSeparator());
		sb.append("    </select>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    <select id=\"query\" parameterType=\"").append(qcClass.getName()).append("\" resultType=\"").append(poClass.getName()).append("\">").append(System.lineSeparator());
		sb.append("        ").append(this.getSelectSql()).append(System.lineSeparator());
		sb.append("        <if test=\"useWhere\"> <!-- 添加查询条件 -->").append(System.lineSeparator());
		sb.append("            where").append(System.lineSeparator());
		sb.append("            <foreach collection=\"conditions\" index=\"index\" item=\"obj\">").append(System.lineSeparator());
		sb.append("                <choose>").append(System.lineSeparator());
		sb.append("                    <when test=\"index == 0\">${obj.field} ${obj.operator} #{obj.value}</when>").append(System.lineSeparator());
		sb.append("                    <otherwise>${obj.join} ${obj.field} ${obj.operator} #{obj.value}</otherwise>").append(System.lineSeparator());
		sb.append("                </choose>").append(System.lineSeparator());
		sb.append("            </foreach>").append(System.lineSeparator());
		sb.append("        </if>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("        <if test=\"useSort\"> <!-- 添加排序 -->").append(System.lineSeparator());
		sb.append("            order by").append(System.lineSeparator());
		sb.append("            <foreach collection=\"sorts\" item=\"obj\" separator=\",\">").append(System.lineSeparator());
		sb.append("                ${obj}").append(System.lineSeparator());
		sb.append("            </foreach>").append(System.lineSeparator());
		sb.append("        </if>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("        <if test=\"page != null\"> <!-- 添加分页 -->").append(System.lineSeparator());
		sb.append("            limit ${page.beginIndex}, ${page.pageSize}").append(System.lineSeparator());
		sb.append("        </if>").append(System.lineSeparator());
		sb.append("    </select>").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    <select id=\"getCount\" parameterType=\"").append(qcClass.getName()).append("\" resultType=\"int\">").append(System.lineSeparator());
		sb.append("        select count(*) from ").append(tableName).append(System.lineSeparator());
		sb.append("        <if test=\"useWhere\">").append(System.lineSeparator());
		sb.append("            where").append(System.lineSeparator());
		sb.append("            <foreach collection=\"conditions\" index=\"index\" item=\"obj\">").append(System.lineSeparator());
		sb.append("                <choose>").append(System.lineSeparator());
		sb.append("                    <when test=\"index == 0\">${obj.field} ${obj.operator} #{obj.value}</when>").append(System.lineSeparator());
		sb.append("                    <otherwise>${obj.join} ${obj.field} ${obj.operator} #{obj.value}</otherwise>").append(System.lineSeparator());
		sb.append("                </choose>").append(System.lineSeparator());
		sb.append("            </foreach>").append(System.lineSeparator());
		sb.append("        </if>").append(System.lineSeparator());
		sb.append("    </select>").append(System.lineSeparator());
		sb.append("    <!--****************************************************************分隔线****************************************************************-->").append(System.lineSeparator());
		sb.append("</mapper>").append(System.lineSeparator());
		return sb.toString();
	}
	
	//生成mapper xml字符串并保存到文件
	public void generateMapperXmlToFile() {
		this.saveStrToFile(generateMapperXmlStr(), new File(this.getMapperFolder(), poClass.getSimpleName() + "Mapper.xml"));
	}
	
	//生成service接口字符串
	public String generateServiceInterfaceSourceStr() {
		StringBuilder sb = new StringBuilder();
		sb.append("package ").append(servicePackage).append(";").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("import java.util.List;").append(System.lineSeparator());
		sb.append("import ").append(poClass.getName()).append(";").append(System.lineSeparator());
		sb.append("import ").append(qcClass.getName()).append(";").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("public interface ").append(poClass.getSimpleName()).append("Service {").append(System.lineSeparator());
		sb.append("    void add(").append(poClass.getSimpleName()).append(" obj);").append(System.lineSeparator());
		sb.append("    void delete(").append(getPoIdTypeName()).append(" id);").append(System.lineSeparator());
		sb.append("    void update(").append(poClass.getSimpleName()).append(" obj);").append(System.lineSeparator());
		sb.append("    ").append(poClass.getSimpleName()).append(" find(").append(qcClass.getSimpleName()).append(" qc);").append(System.lineSeparator());
		sb.append("    ").append(poClass.getSimpleName()).append(" findById(").append(getPoIdTypeName()).append(" id);").append(System.lineSeparator());
		sb.append("    ").append("List<").append(poClass.getSimpleName()).append(">").append(" query(").append(qcClass.getSimpleName()).append(" qc);").append(System.lineSeparator());
		sb.append("    ").append("int getCount(").append(qcClass.getSimpleName()).append(" qc);").append(System.lineSeparator());
		sb.append("}").append(System.lineSeparator());
		return sb.toString();
	}
	
	//生成service接口字符串并保存到文件
	public void generateServiceInterfaceToFile() {
		this.saveStrToFile(generateServiceInterfaceSourceStr(), new File(this.getServiceFolder(), poClass.getSimpleName() + "Service.java"));
	}
	
	//生成service 实现类字符串
	public String generateServiceImplClassSourceStr() {
		StringBuilder sb = new StringBuilder();
		sb.append("package ").append(serviceImplPackage).append(";").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("import java.util.List;").append(System.lineSeparator());
		sb.append("import org.springframework.beans.factory.annotation.Autowired;").append(System.lineSeparator());
		sb.append("import org.springframework.stereotype.Repository;").append(System.lineSeparator());
		sb.append("import org.springframework.transaction.annotation.Transactional;").append(System.lineSeparator());
		sb.append("import ").append(mapperPackage).append(".").append(poClass.getSimpleName()).append("Mapper;").append(System.lineSeparator());
		sb.append("import ").append(poClass.getName()).append(";").append(System.lineSeparator());
		sb.append("import ").append(servicePackage).append(".").append(poClass.getSimpleName()).append("Service;").append(System.lineSeparator());
		sb.append("import ").append(qcClass.getName()).append(";").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("@Repository(\"").append(poClass.getSimpleName().substring(0, 1).toLowerCase() + poClass.getSimpleName().substring(1) + "Service").append("\")").append(System.lineSeparator());
		sb.append("@Transactional").append(System.lineSeparator());
		sb.append("public class ").append(poClass.getSimpleName()).append("ServiceImpl implements ").append(poClass.getSimpleName()).append("Service {").append(System.lineSeparator());
		sb.append("    @Autowired").append(System.lineSeparator());
		sb.append("    private ").append(poClass.getSimpleName()).append("Mapper mapper;").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    @Override").append(System.lineSeparator());
		sb.append("    public void add(").append(poClass.getSimpleName()).append(" obj) {").append(System.lineSeparator());
		sb.append("        mapper.add(obj);").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    @Override").append(System.lineSeparator());
		sb.append("    public void delete(").append(getPoIdTypeName()).append(" id) {").append(System.lineSeparator());
		sb.append("        mapper.delete(id);").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    @Override").append(System.lineSeparator());
		sb.append("    public void update(").append(poClass.getSimpleName()).append(" obj) {").append(System.lineSeparator());
		sb.append("        mapper.update(obj);").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    @Override").append(System.lineSeparator());
		sb.append("    public ").append(poClass.getSimpleName()).append(" find(").append(qcClass.getSimpleName()).append(" qc) {").append(System.lineSeparator());
		sb.append("        return mapper.find(qc);").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    @Override").append(System.lineSeparator());
		sb.append("    public ").append(poClass.getSimpleName()).append(" findById(").append(getPoIdTypeName()).append(" id) {").append(System.lineSeparator());
		sb.append("        return mapper.findById(id);").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    @Override").append(System.lineSeparator());
		sb.append("    public List<").append(poClass.getSimpleName()).append("> query(").append(qcClass.getSimpleName()).append(" qc) {").append(System.lineSeparator());
		sb.append("        return mapper.query(qc);").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("    @Override").append(System.lineSeparator());
		sb.append("    public int getCount(").append(qcClass.getSimpleName()).append(" qc) {").append(System.lineSeparator());
		sb.append("        return mapper.getCount(qc);").append(System.lineSeparator());
		sb.append("    }").append(System.lineSeparator());
		sb.append("}").append(System.lineSeparator());
		return sb.toString();
	}
	
	//生成service 实现类字符串
	public void generateServiceImplClassToFile() {
		this.saveStrToFile(generateServiceImplClassSourceStr(), new File(this.getServiceImplFolder(), poClass.getSimpleName() + "ServiceImpl.java"));
	}
	
	//生成所有到文件
	public void generateAllToFile() {
		generateMapperInterfaceToFile();
		generateMapperXmlToFile();
		generateServiceInterfaceToFile();
		generateServiceImplClassToFile();
	}
	
	//返回po的id的类型名
	public String getPoIdTypeName() {
		Class<?> idType = this.getPoIdType();
		if(idType == int.class) {
			return "int";
		} else if(idType == String.class) {
			return "String";
		} else {
			throw new RuntimeException("id只能是int或String");
		}
	}
	
	//返回po的id的类型
	public Class<?> getPoIdType() {
		Field idField = null;
		try {
			idField = poClass.getDeclaredField("id");
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
		return idField.getType();
	}
	
	//返回Mapper目录
	public String getMapperFolder() {
		String[] strs = mapperPackage.split("\\.");
		String result = javaSourceFolder;
		for(String str : strs) {
			result += File.separator + str;
		}
		return result;
	}
	
	//返回Service目录
	public String getServiceFolder() {
		String[] strs = servicePackage.split("\\.");
		String result = javaSourceFolder;
		for(String str : strs) {
			result += File.separator + str;
		}
		return result;
	}
	
	//返回ServiceImpl目录
	public String getServiceImplFolder() {
		String[] strs = serviceImplPackage.split("\\.");
		String result = javaSourceFolder;
		for(String str : strs) {
			result += File.separator + str;
		}
		return result;
	}
	
	//生成insert sql
	public String getInsertSql() {
		Field[] fields = poClass.getDeclaredFields();
		StringBuilder result = new StringBuilder();
		result.append("insert into ").append(tableName).append("(");
		
		StringBuilder sb1 = new StringBuilder();
		StringBuilder sb2 = new StringBuilder();
		for(int i=0; i<fields.length; i++) {
			sb1.append(fields[i].getName());
			sb2.append("#{").append(fields[i].getName()).append("}");
			if(i < fields.length - 1) {
				sb1.append(", ");
				sb2.append(", ");
			}
		}
		
		result.append(sb1).append(")").append(System.lineSeparator()).append("        ").append("values(").append(sb2).append(")");
		return result.toString();
	}
	
	//生成update sql
	public String getUpdateSql() {
		Field[] fields = poClass.getDeclaredFields();
		StringBuilder result = new StringBuilder();
		result.append("update ").append(tableName).append(" set").append(System.lineSeparator()).append("        ");
		for(int i=0; i<fields.length; i++) {
			result.append(fields[i].getName()).append(" = ").append("#{").append(fields[i].getName()).append("}");
			if(i < fields.length - 1) {
				result.append(", ");
			}
		}
		result.append(System.lineSeparator()).append("        ").append("where id = #{id}");
		return result.toString();
	}
	
	//生成select sql
	public String getSelectSql() {
		Field[] fields = poClass.getDeclaredFields();
		StringBuilder result = new StringBuilder();
		result.append("select ");
		for(int i=0; i<fields.length; i++) {
			result.append(fields[i].getName());
			if(i < fields.length - 1) {
				result.append(", ");
			}
		}
		result.append(" from ").append(tableName);
		return result.toString();
	}
	
	//把字符串写入到文件
	private void saveStrToFile(String str, File file) {
		try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
			bw.write(str);
			bw.flush();
			logger.debug("已生成：" + file.getPath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}