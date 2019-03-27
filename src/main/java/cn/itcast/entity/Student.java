package cn.itcast.entity;

import java.io.Serializable;

public class Student implements Serializable {
	private static final long serialVersionUID = -5304386891883937131L;

	private Integer sid;
	private String sname;
	private Integer age;
	private String gender;
	private Teacher teacher;
	
	public Student() {
		super();
	}

	public Student(Integer sid, String sname, Integer age, String gender) {
		super();
		this.sid = sid;
		this.sname = sname;
		this.age = age;
		this.gender = gender;
	}

	public Integer getSid() {
		return sid;
	}

	public void setSid(Integer sid) {
		this.sid = sid;
	}

	public String getSname() {
		return sname;
	}

	public void setSname(String sname) {
		this.sname = sname;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Teacher getTeacher() {
		return teacher;
	}

	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}

	@Override
	public String toString() {
		return "Student [sid=" + sid + ", sname=" + sname + ", age=" + age + ", gender=" + gender + ", teacher="
				+ teacher + "]";
	}

}
