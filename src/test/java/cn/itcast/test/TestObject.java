package cn.itcast.test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestObject {
	private String id;
	private Integer[] arr;
	private Map<String, String> map;
	private List<String> list;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer[] getArr() {
		return arr;
	}

	public void setArr(Integer[] arr) {
		this.arr = arr;
	}

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(Map<String, String> map) {
		this.map = map;
	}

	public List<String> getList() {
		return list;
	}

	public void setList(List<String> list) {
		this.list = list;
	}

	@Override
	public String toString() {
		return "TestObject [id=" + id + ", arr=" + Arrays.toString(arr) + ", map=" + map + ", list=" + list + "]";
	}

}
