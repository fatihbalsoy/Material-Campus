package us.plxhack.InfiniteCampus.api.calendar;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;

public class Calendar
{
	public String name;
	public String schoolID;
	public String calendarID;
	public String endYear;
	
	public List<ScheduleStructure> schedules = new ArrayList<ScheduleStructure>();
	public Calendar(Element calendar)
	{
		name = calendar.getAttributeValue("calendarName");
		schoolID = calendar.getAttributeValue("schoolID");
		calendarID = calendar.getAttributeValue("calendarID");
		endYear = calendar.getAttributeValue("endYear");
        System.out.println("Calendar info string: " + getInfoString());
		for(int i = 0; i < calendar.getChildElements().size(); i++)
			schedules.add(new ScheduleStructure(calendar.getChildElements().get(i)));
	}

	public Calendar(String name)
	{
		this.name = name;
	}
	
	public String getInfoString()
	{
		String returnString = "Information for Calendar \'" + name + "\':\nSchool ID: " + schoolID + "\nCalendar ID: " + calendarID + "\nEnding Year: " + endYear + "\n\n===Schedules===";
		
		for(ScheduleStructure s : schedules)
			returnString += "\n" + s.getInfoString();
		
		return returnString;
	}
}
