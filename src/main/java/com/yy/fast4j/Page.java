package com.yy.fast4j;

import java.util.ArrayList;
import java.util.List;

public class Page {
	private int pageSize;  //每页显示的记录数
	private int pageNo = 1;    //当前页
	private Integer rowCount;  //总记录数
	private int showCount = 5; //显示当前页，前几页，或后几页的页数，默认为5
	
	public Page(int pageSize) {
		setPageSize(pageSize);
	}
	
	public Page(int pageSize, int pageNo) {
		this(pageSize);
		setPageNo(pageNo);
	}
	
	public Page(int pageSize, int pageNo, int showCount) {
		this(pageSize, pageNo);
		this.showCount = showCount;
	}
	
	//设置pageSize
	public Page setPageSize(int pageSize) {
		if(pageSize <= 0) {
			throw new RuntimeException("pageSize必须大于零");
		}
		this.pageSize = pageSize;
		return this;
	}
	
	//设置pageNo
	public Page setPageNo(int pageNo) {
		if(pageNo <= 0) {
			throw new RuntimeException("pageNo必须大于零");
		}
		this.pageNo = pageNo;
		return this;
	}
	
	//设置rowCount
	public Page setRowCount(Integer rowCount) {
		this.rowCount = rowCount;
		return this;
	}
	
	//设置showCount
	public Page setShowCount(int showCount) {
		this.showCount = showCount;
		return this;
	}
	
	//计算查询起始索引，从0开始
	public int getBeginIndex() {
		return (this.pageNo - 1) * this.pageSize;
	}
	
	//返回pageSize;
	public int getPageSize() {
		return pageSize;
	}
	//返回pageNo
	public int getPageNo() {
		return pageNo;
	}
	//返回rowCount
	public Integer getRowCount() {
		return rowCount;
	}
	//返回showCount
	public int getShowCount() {
		return showCount;
	}
	
	//得到总页数
	public int getPageCount() {
		if(this.rowCount == null) {
			throw new RuntimeException("rowCount为null，无法计算总页数");
		}
		if(this.rowCount % this.pageSize == 0) {
			return this.rowCount / this.pageSize;
		} else {
			return this.rowCount / this.pageSize + 1;
		}
	}
	
	//是否有下一页
	public boolean isNext() {
		return this.pageNo < this.getPageCount();
	}
	//得到后几页的页数
	public List<Integer> getNextPages() {
		List<Integer> list = new ArrayList<Integer>();
		for(int i = this.pageNo + 1; i <= this.pageNo + this.showCount && i <= this.getPageCount(); i++) {
			list.add(i);
		}
		return list;
	}
	
	//是否有上一页
	public boolean isPrevious() {
		return this.pageNo > 1;
	}
	//得到前几页的页数
	public List<Integer> getPreviousPages() {
		List<Integer> list = new ArrayList<Integer>();
		for(int i = this.pageNo - this.showCount; i < this.pageNo; i++) {
			if(i > 0) {
				list.add(i);
			}
		}
		return list;
	}
}