/*
 * Copyright 2002-2013 SCOOP Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.copperengine.core.persistent.alpha.generator;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;


public class PersisterFactoryGenerator {
	
	public static class SqlType {
		
		public int       sqlType; /* from java.sql.Types */
		public Integer   length;
		public Integer   precision;
		public boolean   nullable;
		
		public String toTypesReference() {
			if (sqlType == Types.NULL)
				return "UNKNOWN_SQL_TYPE";
			for (Field field : Types.class.getDeclaredFields()) {
				if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers()))
					continue;
				if (field.getType() != int.class)
					continue;
				try {
					if (field.getInt(null) == sqlType)
						return "java.sql.Types."+field.getName();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
			return "UNKNOWN_SQL_TYPE";
		}
		
		public String toColumnDefinition() {
			String def = "UNKNOWN";
			switch (sqlType) {
			case Types.VARCHAR:
				def = "VARCHAR2("+length+")"; break;
			case Types.TIME:
			case Types.DATE:
				def = "DATE"; break;
			case Types.TIMESTAMP:
				return "TIMESTAMP";
			case Types.NUMERIC: {
				    if (length != null) {
				    	if (precision != null) {
				    		def = "NUMBER("+length+","+precision+")"; break;
				    	}
			    		def = "NUMBER("+length+")"; break;
				    }
		    		def = "NUMBER"; break;				
				}
			}
			def = def + (nullable?" NULL":" NOT NULL");
			return def;
		}

	}
	
	public static class PersistentMember {
		
		public String    setter;
		public String    getter;
		public String    columnName;
		public SqlType   sqlType; /* from java.sql.Types */
		public Class<?>  javaType;
		
		public static PersistentMember fromProperty(PropertyDescriptor desc) {
			PersistentMember p = new PersistentMember();
			p.setter = desc.getWriteMethod().getName();
			p.getter = desc.getReadMethod().getName();
			p.columnName = desc.getName().toUpperCase();
			p.javaType = desc.getPropertyType();
			p.sqlType = translateSqlType(desc.getPropertyType());
			return p;
		}

		public static PersistentMember fromProperty(Class<?> declaringClass, String propertyName) {
			try {
				for (PropertyDescriptor desc : Introspector.getBeanInfo(declaringClass).getPropertyDescriptors()) {
					if (propertyName.equals(desc.getName())) {
						return fromProperty(desc);
					}
				}
			} catch (IntrospectionException e) {
				throw new RuntimeException(e);
			}
			throw new RuntimeException("No such property: "+declaringClass.getCanonicalName()+"."+propertyName);
		}

	}
	
	
	public static class GenerationDescription  {
		
		final String entitySimpleName;
		final String entityPackageName;
		final String factoryPackgeName;
		final String tableName;
		final ArrayList<PersistentMember> persistentMembers = new ArrayList<PersistentMember>();
		
		public GenerationDescription(		String entitySimpleName,String entityPackageName,String factoryPackgeName) {
			this.entityPackageName = entityPackageName;
			this.entitySimpleName = entitySimpleName;
			this.factoryPackgeName = factoryPackgeName;
			this.tableName = this.entitySimpleName.toUpperCase();
		}
		
		public String getEntitySimpleName() {
			return entitySimpleName;
		}
		public String getEntityPackageName() {
			return entityPackageName;
		}
		public String getFactoryPackgeName() {
			return factoryPackgeName;
		}
		public String getTableName() {
			return tableName;
		}
		public ArrayList<PersistentMember> getPersistentMembers() {
			return persistentMembers;
		}
		
	}
	
	static class Region {
		int start;
		int end;
		String linePrefix; 
	}
	
	
	public String getColumnList(GenerationDescription desc, String alias) {
		StringBuilder sb = new StringBuilder(alias).append(".\\\"WORKFLOWID\\\", ")
				                     .append(alias).append(".\\\"ENTITYID\\\"");
		alias = alias != null && !"".equals(alias)?alias+".":"";
		if (desc.getPersistentMembers().iterator().hasNext()) {
			sb.append(", ").append(getColumnListForPersistentMembers(desc, alias+"\\\"","\\\"",", "));
		}
		return sb.toString();
	}



	public static SqlType translateSqlType(Class<?> javaType) {
		SqlType t = new SqlType();
		t.sqlType = Types.NULL;
		t.nullable = javaType.isPrimitive();
		if (javaType == String.class) {
			t.sqlType = Types.VARCHAR;
			t.length = 1024;
		} else if (Time.class.isAssignableFrom(javaType)) {
			t.sqlType = Types.TIME;
		} else if (Timestamp.class.isAssignableFrom(javaType)) {
			t.sqlType = Types.TIMESTAMP;
		} else if (Date.class.isAssignableFrom(javaType)) {
			t.sqlType = Types.DATE;
		} else if (javaType.isPrimitive() || isBoxedPrimitiveType(javaType)) {
			javaType = isBoxedPrimitiveType(javaType)?getPrimitiveJavaType(javaType):javaType;
			t.sqlType = javaType == boolean.class?Types.CHAR:Types.NUMERIC;
			if (javaType == boolean.class) {
				t.length = 1;
			} else if (javaType == byte.class) {
				t.length = 3;
			} else if (javaType == short.class) {
				t.length = 5;
			} else if (javaType == int.class) {
				t.length = 10;
			} else if (javaType == long.class) {
				t.length = 19;
			} else if (javaType == float.class || javaType == double.class) {
				/* NUMBER Default */
			}
		}
		return t;
	}



	private String getColumnListForPersistentMembers(GenerationDescription desc,
			String prefix, String postfix, String separator) {
		if (!desc.getPersistentMembers().iterator().hasNext())
			return "";
		StringBuilder sb = new StringBuilder();
		for (PersistentMember mem : desc.getPersistentMembers()) {
			sb.append(prefix).append(mem.columnName).append(postfix).append(separator);
		}
		sb.setLength(sb.length()-separator.length());
		return sb.toString();
	}
	
	
	

	private String getColumnDefinitions(GenerationDescription desc,
			String linePrefix) {
		StringBuilder sb = new StringBuilder();
		for (PersistentMember mem : desc.getPersistentMembers()) {
			sb.append(linePrefix).append('"').append(mem.columnName).append("\" ").append(' ').append(mem.sqlType.toColumnDefinition()).append(',');
		}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}



	private String getQMarkList(int i) {
		StringBuilder sb = new StringBuilder();
		for (int j = 0; j < i; ++j) {
			sb.append("?,");
		}
		sb.setLength(sb.length()-1);
		return sb.toString();
	}



	private String getResultSetGettersForPersistentMembers(
			GenerationDescription desc, int i, String linePrefix) {
		StringBuilder sb = new StringBuilder();
		for (PersistentMember mem : desc.getPersistentMembers()) {
			boolean handleNull = mem.sqlType.nullable&&!mem.javaType.isPrimitive();
			if (handleNull) {
				sb.append(linePrefix).append(mem.javaType.getCanonicalName()).append(" _").append(i).append(" = rs.").append(getResultSetGetter(mem.javaType)).append("(").append(i).append(");");
				sb.append(linePrefix).append("if (rs.wasNull()) {")
					.append(linePrefix).append("\tentity.").append(mem.setter).append("(_").append(i).append(");")
					.append(linePrefix).append("} else {")
					.append(linePrefix).append("\tentity.").append(mem.setter).append("(null);")
					.append(linePrefix).append("}");
			} else {
				sb.append(linePrefix).append("entity.").append(mem.setter).append("(rs.").append(getResultSetGetter(mem.javaType)).append('(').append(i).append("));");				
			}
			++i;
		}
		return sb.toString();
	}
	
	private String getStatementSettersForPersistentMembers(
			GenerationDescription desc, int i, String linePrefix) {
		StringBuilder sb = new StringBuilder();
		for (PersistentMember mem : desc.getPersistentMembers()) {
			boolean handleNull = mem.sqlType.nullable&&!mem.javaType.isPrimitive();
			if (handleNull) {
				sb.append(linePrefix).append("if (entity.").append(mem.getter).append("() == null) {")
				  .append(linePrefix).append("\tstmt.setNull(").append(i).append(",").append(mem.sqlType.toTypesReference()).append(");")
				  .append(linePrefix).append("} else {")
				   .append(linePrefix).append("\tstmt.").append(getStatementSetter(mem.javaType)).append('(').append(i).append(", entity.").append(mem.getter).append("());")
				   .append(linePrefix).append("}");
			} else {
				 sb.append(linePrefix).append("stmt.").append(getStatementSetter(mem.javaType)).append('(').append(i).append(", entity.").append(mem.getter).append("());");
			}
			++i;
		}
		return sb.toString();
	}

	private String getCompleteStatementSetters (
			GenerationDescription desc, String linePrefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(linePrefix).append("stmt.setString( 1, en.workflow.getId());");
		sb.append(linePrefix).append("stmt.setString( 2, entity.getEntityId());");
		sb.append(getStatementSettersForPersistentMembers(desc,3,linePrefix));
		return sb.toString();
	}



	private String getResultSetGetter(Class<?> javaType) {
		if (javaType == String.class)
			return "getString";
		if (Time.class.isAssignableFrom(javaType))
			return "getDate";
		if (Timestamp.class.isAssignableFrom(javaType))
			return "getTimestamp";
		if (Date.class.isAssignableFrom(javaType))
			return "getDate";
		if (javaType.isPrimitive() || isBoxedPrimitiveType(javaType)) {
			String type = (isBoxedPrimitiveType(javaType)?getPrimitiveJavaType(javaType):javaType).getSimpleName();
			return "get"+Character.toUpperCase(type.charAt(0))+type.substring(1);
		}
		return "getCANNOT_DETERMINE_GETTER";
	}

	private String getStatementSetter(Class<?> javaType) {
		if (javaType == String.class)
			return "setString";
		if (Time.class.isAssignableFrom(javaType))
			return "setDate";
		if (Timestamp.class.isAssignableFrom(javaType))
			return "setTimestamp";
		if (Date.class.isAssignableFrom(javaType))
			return "setDate";
		if (javaType.isPrimitive()) {
			String type = (isBoxedPrimitiveType(javaType)?getPrimitiveJavaType(javaType):javaType).getSimpleName();
			return "set"+Character.toUpperCase(type.charAt(0))+type.substring(1);
		}
		return "setCANNOT_DETERMINE_SETTER";
	}


	private static boolean isBoxedPrimitiveType(Class<?> javaType) {
		return Arrays.<Class<?>>asList(Byte.class,Short.class,Integer.class,Long.class,Boolean.class,Float.class,Double.class).contains(javaType);
	}

	private static Class<?> getPrimitiveJavaType(Class<?> javaType) {
		if (javaType == Boolean.class)
			return boolean.class;
		if (javaType == Float.class)
			return float.class;
		if (javaType == Double.class)
			return double.class;
		if (javaType == Byte.class)
			return byte.class;
		if (javaType == Short.class)
			return short.class;
		if (javaType == Integer.class)
			return int.class;
		if (javaType == Long.class)
			return long.class;
		throw new RuntimeException("No primitive type for "+javaType.getCanonicalName());
	}



	private String replace(String source, Region reg, String replacement) {
		return source.substring(0,reg.start)+replacement+source.substring(reg.end);
	}



	private Region findRegion(String template, String regionName) {
		
		String startMarker = "/*BEGIN"+regionName+"*/";
		String endMarker = "/*END"+regionName+"*/";
		Region reg = new Region();
		reg.start = template.indexOf(startMarker);
		if (reg.start < 0)
			return null;
		int idx = reg.start;
		StringBuilder prefix = new StringBuilder();
		for (;--idx > -1;) {
			if (Character.isWhitespace(template.charAt(idx))) {
				prefix.append(template.charAt(idx));
			} else {
				break;
			}
		}
		reg.linePrefix = prefix.reverse().toString();
		reg.end = template.indexOf(endMarker,reg.start)+endMarker.length();
		return reg;

	}


	private StringBuilder readFully(InputStream i) throws IOException {
		StringBuilder sb = new StringBuilder();
		byte[] buf = new byte[4096];
		int c = 0;
		while ((c = i.read(buf)) > 0) {
			sb.append(new String(buf,0,c,"UTF-8"));
		}
		return sb;
	}
	
	
	public void generatePersisterFactory(GenerationDescription desc, Writer output) throws UnsupportedEncodingException, IOException {
		
		String sourcePackageName = getClass().getPackage().getName();
		String source = "/"+sourcePackageName.replace('.','/')+"/TEMPLATEPersisterFactory._java";
		InputStream i = getClass().getResourceAsStream(source);
		String template = readFully(i).toString();
		template = template.replaceAll("TEMPLATE", desc.getEntitySimpleName());
		template = template.replaceAll("/\\*ADDITIONAL_IMPORTS\\*/", "import "+desc.getEntityPackageName()+"."+desc.getEntitySimpleName()+";");
		template = template.replaceAll("package "+sourcePackageName+";", "package "+desc.getFactoryPackgeName()+";");
		
		Region reg = findRegion(template, "SQL_SELECTORACLE");
		String selectStmt = "\"SELECT "+getColumnList(desc, "tab")+"\"+"
		                     +reg.linePrefix+"\"FROM \\\""+desc.getTableName()+"\\\" as tab, TABLE(:1) as IDS\"+"
		                     +reg.linePrefix+"\"WHERE tab.\\\"WORKFLOWID\\\" =  IDS.COLUMN_VALUE\"";
		template = replace(template,reg,selectStmt);
		
		reg = findRegion(template, "SQL_SELECTCOMMON");
		selectStmt = "\"SELECT "+getColumnList(desc, "tab")+"\"+"
		                     +reg.linePrefix+"\"FROM \\\""+desc.getTableName()+"\\\" as tab\"+"
		                     +reg.linePrefix+"\"WHERE tab.\\\"WORKFLOWID\\\" IN (\"";
		template = replace(template,reg,selectStmt);
		
		for (;;) {
			reg = findRegion(template, "SQL_SELECT_GETMEMBERS");
			if (reg == null)
				break;
			template = replace(template,reg,getResultSetGettersForPersistentMembers(desc,3,reg.linePrefix));
		}

		reg = findRegion(template, "SQL_INSERT");
		String insertStmt = "\"INSERT INTO \\\""+desc.getTableName()+"\\\"\"+"
		                     +reg.linePrefix+"\"\t("+getColumnList(desc, "")+")\"+"
		                     +reg.linePrefix+"\"\tVALUES("+getQMarkList(desc.getPersistentMembers().size()+2)+")\"";
		template = replace(template,reg,insertStmt);

		reg = findRegion(template, "SQL_INSERT_SETMEMBERS");
		template = replace(template,reg,getCompleteStatementSetters(desc,reg.linePrefix));

		reg = findRegion(template, "SQL_UPDATE");
		String updateStmt = "\"UPDATE \\\""+desc.getTableName()+"\\\"\"+"
		                     +reg.linePrefix+"\"\tSET "+getColumnListForPersistentMembers(desc, "","= ?",", ")+"\"+"
		                     +reg.linePrefix+"\"\tWHERE \\\"WORKFLOWID\\\"=? AND \\\"ENTITYID\\\" = ?\"";
		template = replace(template,reg,updateStmt);

		reg = findRegion(template, "SQL_UPDATE_SETMEMBERS");
		String setters = getStatementSettersForPersistentMembers(desc,1,reg.linePrefix)
				+reg.linePrefix+"stmt.setString("+(desc.getPersistentMembers().size()+1)+", en.workflow.getId());"
				+reg.linePrefix+"stmt.setString("+(desc.getPersistentMembers().size()+2)+", entity.getEntityId());";
		template = replace(template,reg,setters);

		reg = findRegion(template, "SQL_DELETE");
		String deleteStmt = "\"DELETE FROM \\\""+desc.getTableName()+"\\\"\"+"
		                     +reg.linePrefix+"\"\tWHERE \\\"WORKFLOWID\\\"=? AND \\\"ENTITYID\\\" = ?\"";
		template = replace(template,reg,deleteStmt);

		output.append(template);
		output.flush();
	}

	public void generateSqlCreateTable(GenerationDescription desc, Writer output) throws UnsupportedEncodingException, IOException {

		String sourcePackageName = getClass().getName();
		String source = "/"+sourcePackageName.replace('.','/')+"/TEMPLATE.sql";
		InputStream i = getClass().getResourceAsStream(source);
		String template = readFully(i).toString();
		template = template.replaceAll("TEMPLATE", desc.getTableName());
		
		Region reg = findRegion(template, "COLUMNS");
		template = replace(template,reg,getColumnDefinitions(desc,reg.linePrefix));
		
		output.append(template);
		output.flush();
	}
	

	public static void main(String[] args) throws UnsupportedEncodingException, IOException, IntrospectionException {
		GenerationDescription impl = new GenerationDescription("TEMPLATE","org.copperengine.core.core.persistent.alpha.generator","org.copperengine.core.core.persistent.alpha.generator");
		for (PropertyDescriptor desc : Introspector.getBeanInfo(TEMPLATE.class).getPropertyDescriptors()) {
			if ("entityId".equals(desc.getName()))
				continue;
			if (desc.getWriteMethod() != null && desc.getReadMethod() != null) {
				impl.getPersistentMembers().add(PersistentMember.fromProperty(desc));
			}
		}
		new PersisterFactoryGenerator().generatePersisterFactory(impl, new OutputStreamWriter(System.out));
		new PersisterFactoryGenerator().generateSqlCreateTable(impl, new OutputStreamWriter(System.out));
	}

}
