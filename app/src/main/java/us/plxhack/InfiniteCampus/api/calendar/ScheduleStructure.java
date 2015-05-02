package us.plxhack.InfiniteCampus.api.calendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nu.xom.Element;

public class ScheduleStructure
{
	public String id;
	public String name;
	public String label;
	public String grade;
	public boolean active;
	public String primary;
	public boolean is_default;
	public Date startDate;
	public ScheduleStructure(Element sceduleElement)
	{
		id = sceduleElement.getAttributeValue("structureID");
		name = sceduleElement.getAttributeValue("structureName");
		label = sceduleElement.getAttributeValue("label");
		grade = sceduleElement.getAttributeValue("grade");
		active = sceduleElement.getAttributeValue("active").equalsIgnoreCase("true");
		primary = sceduleElement.getAttributeValue("primary");
        if(sceduleElement.getAttributeValue("default") != null)
        {
            is_default = sceduleElement.getAttributeValue("default").equalsIgnoreCase("true");
        }
        else
        {
            is_default = false;
        }

		try
		{
			startDate = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).parse(sceduleElement.getAttributeValue("startDate"));
		}
		catch(Exception e)
		{
			startDate = new Date();
		}
	}

	public ScheduleStructure(String name)
	{
		this.name = name;
	}
	
	public String getInfoString()
	{
		return "Information for Schedule \'" + name  + "\' titled \'" + label + "\':\nGrade: " + grade + "\nID: " + id + "\nIs Active? " + active + "\nPrimary: " + primary + "\nIs Default? " + is_default + "\nEnding Date: " + startDate.toString();
	}
}
