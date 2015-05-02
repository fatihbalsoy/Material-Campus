package us.plxhack.InfiniteCampus.api;

import java.util.ArrayList;
import java.util.List;

import us.plxhack.InfiniteCampus.api.calendar.Calendar;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Course;
import us.plxhack.InfiniteCampus.api.district.DistrictInfo;
import nu.xom.*;

public class Student
{
	public String studentNumber;
	public boolean hasSecurityRole = false;
	public String personID;
	public String lastName;
	public String firstName;
	public String middleName;
	public String isGuardian;
	
	public List<Calendar> calendars = new ArrayList<Calendar>();
	public List<Course> courses = new ArrayList<Course>();
	public List<Activity> newAssignments = new ArrayList<>();
	
	private DistrictInfo distInfo;
	
	public Student(Element userElement)
	{
		this(userElement, null);
	}
	
	public Student(Element userElement, DistrictInfo info)
	{
		distInfo = info;
		
		studentNumber = userElement.getAttributeValue("studentNumber");
		personID = userElement.getAttributeValue("personID");
		lastName = userElement.getAttributeValue("lastName");
		firstName = userElement.getAttributeValue("firstName");
		middleName = userElement.getAttributeValue("middleName");
		isGuardian = userElement.getAttributeValue("isGuardian");
		for(int i = 0; i < userElement.getChildElements("Calendar").size(); i++)
			calendars.add(new Calendar(userElement.getChildElements("Calendar").get(i)));
	}

	public Student(String name)
	{
		this.firstName = name;
	}

	public String getPictureURL()
	{
		return distInfo.getDistrictBaseURL() + "personPicture.jsp?personID=" + personID;
	}
	
	//TODO: Load news items
	public String getInfoString()
	{
		String userInfo = "Information for " + firstName + " " + middleName + " " + lastName + ":\nStudent Number: " + studentNumber + "\nPerson ID: " + personID + "\nPicture URL: " + getPictureURL() + "\nIs Guardian? " + isGuardian + "\n\n===Calendars===";
		
		for(Calendar c : calendars)
			userInfo += "\n" + c.getInfoString();

		return userInfo;
	}
}
