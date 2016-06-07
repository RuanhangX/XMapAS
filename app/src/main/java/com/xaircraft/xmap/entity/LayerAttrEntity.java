package com.xaircraft.xmap.entity;

import EMap.IO_GisDB.IEGisDB_Base.ATT_FIELD_TYPE;

public class LayerAttrEntity {
	
		private String name;
		private ATT_FIELD_TYPE type;
		private String defaultValue;
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public ATT_FIELD_TYPE getType() {
			return type;
		}
		public void setType(ATT_FIELD_TYPE type) {
			this.type = type;
		}
		public String getDefaultValue() {
			return defaultValue;
		}
		public void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}
		
}
