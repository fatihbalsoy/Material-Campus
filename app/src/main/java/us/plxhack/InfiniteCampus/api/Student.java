package us.plxhack.InfiniteCampus.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import us.plxhack.InfiniteCampus.api.calendar.Calendar;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Course;
import us.plxhack.InfiniteCampus.api.course.Teacher;
import us.plxhack.InfiniteCampus.api.district.DistrictInfo;
import nu.xom.*;

public class Student
{
	private String studentNumber;
	private boolean hasSecurityRole = false;
	private String personID;
	private String lastName;
	private String firstName;
	private String middleName;
	private String isGuardian;
	
	private List<Calendar> calendars = new ArrayList<Calendar>();
	private List<Course> courses = new ArrayList<Course>();
	private List<Activity> newAssignments = new ArrayList<>();
	
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

	public Student(JSONObject json) throws JSONException
	{
		firstName = json.getString("firstName");
		middleName = json.getString("middleName");
		lastName = json.getString("lastName");
		personID = json.getString("id");
		studentNumber = json.getString("number");
		hasSecurityRole = json.getBoolean("security");
		isGuardian = json.getString("guardian");

		JSONArray jsonCourses = json.getJSONArray("courses");
		courses = new ArrayList<>();
		for(int i = 0; i < jsonCourses.length(); i++)
		{
			courses.add(new Course(jsonCourses.getJSONObject(i)));
		}

		JSONArray jsonCalendars = json.getJSONArray("calendars");
		calendars = new ArrayList<>();
		for(int i = 0; i < jsonCalendars.length(); i++)
		{
			calendars.add(new Calendar(jsonCalendars.getJSONObject(i)));
		}
	}
	
	public String getPictureURL()
	{
		return distInfo.getDistrictBaseURL() + "personPicture.jsp?personID=" + personID;
	}
	
	public void updateCourses(List<Course> newCourses, List<Activity> updated)
	{
		courses = new ArrayList<>( );
		newAssignments = updated;

		//alphabetically sort course list
		for (int i=0;i < newCourses.size();++i)
		{
			if (courses.size() == 0)
			{
				courses.add( newCourses.get(i) );
			}
			else
			{
				int placement = -1;

				for (int j=0;j < courses.size();++j)
				{
					if (newCourses.get(i).getCourseName().compareTo(courses.get(j).getCourseName()) < 0 && placement == -1)
					{
						placement = j;
						break;
					}
				}
				if (placement != -1)
					courses.add( placement, newCourses.get(i) );
				else
					courses.add( newCourses.get(i) );
			}
		}
	}
	
	//TODO: Load news items
	public String getInfoString()
	{
		String userInfo = "Information for " + firstName + " " + middleName + " " + lastName + ":\nStudent Number: " + studentNumber + "\nPerson ID: " + personID + "\nPicture URL: " + getPictureURL() + "\nIs Guardian? " + isGuardian + "\n\n===Calendars===";
		
		for(Calendar c : calendars)
			userInfo += "\n" + c.getInfoString();

		return userInfo;
	}

	public String getStudentNumber()
	{
		return studentNumber;
	}
	public String getPersonID()
	{
		return personID;
	}
	public String getLastName()
	{
		return lastName;
	}
	public String getFirstName()
	{
		return firstName;
	}
	public String getMiddleName()
	{
		return middleName;
	}
	public String isGuardian()
	{
		return isGuardian;
	}
	public boolean isInSecurityRole()
	{
		return hasSecurityRole;
	}
	public List<Calendar> getCalendars()
	{
		return calendars;
	}
	public List<Course> getCourses()
	{
		return courses;
	}
	public List<Activity> getNewAssignments()
	{
		return newAssignments;
	}
}
