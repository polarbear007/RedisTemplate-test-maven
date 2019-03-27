package cn.itcast.entity;

import java.io.Serializable;

public class Teacher implements Serializable {
	private static final long serialVersionUID = 146274999216277403L;

	private Integer tid;
	private String tname;
	private String gender;
	private Integer age;

	public Teacher() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Teacher(Integer tid, String tname, String gender, Integer age) {
		super();
		this.tid = tid;
		this.tname = tname;
		this.gender = gender;
		this.age = age;
	}

	public Integer getTid() {
		return tid;
	}

	public void setTid(Integer tid) {
		this.tid = tid;
	}

	public String getTname() {
		return tname;
	}

	public void setTname(String tname) {
		this.tname = tname;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	@Override
	public String toString() {
		return "Teacher [tid=" + tid + ", tname=" + tname + ", gender=" + gender + ", age=" + age + "]";
	}

}
