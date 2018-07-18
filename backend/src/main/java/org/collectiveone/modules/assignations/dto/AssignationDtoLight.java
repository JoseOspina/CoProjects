package org.collectiveone.modules.assignations.dto;

import java.util.ArrayList;
import java.util.List;

public class AssignationDtoLight {
	
	private String id;
	private String type;
	private String motive;
	private String notes;
	private String state;
	private String modelSectionId;
	private String modelSectionName;

	public String getModelsectionid()
	{
		return this.modelSectionId;
	}

	public void setModelsectionid(String modelSectionId)
	{
		this.modelSectionId = modelSectionId;
	}

	private String modelSectionName;

	public String getModelsectionName()
	{
		return this.modelSectionName;
	}

	public void setModelsectionName(String modelSectionName)
	{
		this.modelSectionName = modelSectionName;
	}

	private Long creationDate;
	private List<BillDto> assets = new ArrayList<BillDto>();
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMotive() {
		return motive;
	}
	public void setMotive(String motive) {
		this.motive = motive;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
<<<<<<< HEAD
	public String getModelSectionId() {
		return modelSectionId;
