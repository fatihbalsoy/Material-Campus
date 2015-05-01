package us.plxhack.InfiniteCampus.api;

import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Category;
import us.plxhack.InfiniteCampus.api.course.Task;
import us.plxhack.InfiniteCampus.api.district.DistrictInfo;
import us.plxhack.InfiniteCampus.api.course.Course;

public class InfiniteCampusApi
{
	public static DistrictInfo districtInfo;
	public static Student userInfo;
	private static boolean isLoggedIn = false;

    private static String _districtCode, _username, _password;

    private static Context context;

	private static URL getFormattedURL( String document )
	{
		try
		{
			return new URL( getFormattedURLString( document ) );
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	private static String getFormattedURLString( String document )
	{
		return districtInfo.getDistrictBaseURL() + document;
	}
	
	public static boolean isLoggedIn( )
	{
		return isLoggedIn;
	}
	
	public static boolean login( String districtCode, String username, String password, Context con ) throws ParsingException, IOException
    {
        context = con;
		//Get District Information from district code
		CoreManager core = new CoreManager(districtCode);
		districtInfo = core.getDistrictInfo();

        File file = new File(context.getFilesDir(), "existinggrades.data");
        if(!file.exists())
        {
            file.createNewFile();
        }

        List<Activity> newAssignments = new ArrayList<>();
        List<String> existingAssignments = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = "";
        while((line = br.readLine()) != null)
        {
            existingAssignments.add(line.split("-")[0]);
            System.out.println(line.split("-")[0] + " already exists");
        }

		//Try to log in with given district info, username, and pass
		if (!core.attemptLogin(username, password, core.getDistrictInfo()))
        {
            System.out.println("Login Failed!");
            return false;
        }

        Builder builder = new Builder();
        {
            //Get User information from formatted web page
            URL infoURL = getFormattedURL("prism?x=portal.PortalOutline&appName=" + core.getDistrictInfo().getDistrictAppName());
            Document doc = builder.build(new ByteArrayInputStream(core.getContent(infoURL, false).getBytes()));
            Element root = doc.getRootElement();

            if(root == null)
            {
                System.out.println("Root element is null");
            }
            else
            {
                System.out.println("Root element appears fine");
            }
            userInfo = new Student(root.getFirstChildElement("PortalOutline").getFirstChildElement("Family").getFirstChildElement("Student"), core.getDistrictInfo());
        }

        List<Element> roots = new ArrayList<>();
        //Get classbook information from formatted web page
        for(int i = 0; i < userInfo.calendars.size(); i++)
        {
            URL infoURL2 = getFormattedURL("prism?&x=portal.PortalClassbook-getClassbookForAllSections&mode=classbook&personID=" + userInfo.personID + "&structureID=" + userInfo.calendars.get(i).schedules.get(0).id + "&calendarID=" + userInfo.calendars.get(i).calendarID);
            System.out.println("URL: " + infoURL2.toString());
            Document doc2 = builder.build(new ByteArrayInputStream(core.getContent(infoURL2, false).getBytes()));
            Element root = doc2.getRootElement().getFirstChildElement("SectionClassbooks");
            roots.add(root);
        }

        ArrayList<Element> courseElements = new ArrayList<Element>();

        for(int j = 0; j < roots.size(); j++)
        {

            Element root = roots.get(j);
            if(root != null)
            {
                for (int i=0;i < root.getChildCount();++i)
                {
                    Element portalClassbook = root.getChildElements().get(i);
                    Element studentList = portalClassbook.getFirstChildElement("ClassbookDetail").getFirstChildElement("StudentList");

                    if (studentList.getChildCount() != 0)
                    {
                        Element ce = studentList.getChildElements().get(0).getFirstChildElement("Classbook");

                        Element terms = portalClassbook.getFirstChildElement("Calendar").getFirstChildElement("scheduleStructures").getFirstChildElement("ScheduleStructure").getFirstChildElement("termSchedules").getFirstChildElement("TermSchedule").getFirstChildElement("terms");

                        for (int k=0;k < terms.getChildElements().size();++k)
                        {
                            Element term = terms.getChildElements().get(k);

                            if (term.getAttribute("current") != null && term.getAttributeValue("current").equalsIgnoreCase("true"))
                            {
                                ce.addAttribute(new Attribute("current_term", term.getAttributeValue("name")));
                            }
                        }

                        courseElements.add( ce );
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();

        ArrayList<Course> courses = new ArrayList<Course>();

        for (int i=0;i < courseElements.size();++i)
        {
            Element e = courseElements.get(i);

            if(e != null)
            {
                Course c = new Course( Integer.valueOf( e.getAttributeValue("courseNumber") ), e.getAttributeValue("courseName"), e.getAttributeValue("teacherDisplay") );

                Elements taskChildren = e.getFirstChildElement("tasks").getChildElements();
                ArrayList<Element> tasks = new ArrayList<>();

                for (int j=0;j < taskChildren.size();++j)
                {
                    Element t = taskChildren.get(j);

                    if(t != null)
                    {
                        if (taskChildren.get(j).getAttributeValue("name").equalsIgnoreCase("final"))
                        {
                            c.percentage = Float.valueOf( t.getAttributeValue("percentage") );
                            c.letterGrade = t.getAttributeValue("letterGrade");
                        }
                        else if (taskChildren.get(j).getChildCount() != 0 && taskChildren.get(j).getAttributeValue("termName").equalsIgnoreCase( e.getAttributeValue("current_term") ))
                        {
                            tasks.add( t );
                        }
                    }
                }

                for (int l=0;l < tasks.size();++l)
                {
                    Element finalTaskGroups = tasks.get(l).getFirstChildElement("groups");

                    if(finalTaskGroups != null)
                    {
                        Task t = new Task( tasks.get(l).getAttributeValue("name") );
                        t.termName = tasks.get(l).getAttributeValue("termName");
                        t.weight = Float.valueOf(tasks.get(l).getAttributeValue("weight"));
                        t.percentage = Float.valueOf(tasks.get(l).getAttributeValue("percentage"));
                        t.letterGrade = tasks.get(l).getAttributeValue("letterGrade");

                        for (int j = 0; j < finalTaskGroups.getChildCount(); ++j)
                        {
                            Element groupElement = finalTaskGroups.getChildElements().get(j);

                            if(groupElement != null)
                            {
                                Category category = new Category(groupElement.getAttributeValue("name"));
                                category.percentage = Float.valueOf(groupElement.getAttributeValue("percentage"));
                                category.earnedPoints = Float.valueOf(groupElement.getAttributeValue("pointsEarned"));
                                category.totalPoints = Float.valueOf(groupElement.getAttributeValue("totalPointsPossible"));
                                category.weight = Float.valueOf(groupElement.getAttributeValue("weight"));
                                String gradeLetter = groupElement.getAttributeValue("letterGrade");
                                if (gradeLetter != null)
                                    category.letterGrade = gradeLetter;
                                else
                                    category.letterGrade = "N/A";

                                Element activities = groupElement.getFirstChildElement("activities");

                                for (int k = 0; k < activities.getChildCount(); ++k) {
                                    Element activityElement = activities.getChildElements().get(k);

                                    if(activityElement != null)
                                    {
                                        Activity a = new Activity(activityElement.getAttributeValue("name"));
                                        a.percentage = Float.valueOf(activityElement.getAttributeValue("percentage"));
                                        a.earnedPoints = activityElement.getAttributeValue("score");
                                        a.totalPoints = Float.valueOf(activityElement.getAttributeValue("weightedTotalPoints"));
                                        a.dueDate = activityElement.getAttributeValue("dueDate");
                                        a.missing = Boolean.valueOf(activityElement.getAttributeValue("missing"));
                                        a.id = activityElement.getAttributeValue("activityID");
                                        a.className = c.getCourseName();
                                        String letterGrade = activityElement.getAttributeValue("letterGrade");
                                        if (letterGrade != null)
                                            a.letterGrade = letterGrade;
                                        else
                                            a.letterGrade = "N/A";

                                        boolean found = false;
                                        for(int o = 0; o < existingAssignments.size(); o++)
                                        {
                                            if(existingAssignments.get(o).equals(a.id))
                                            {
                                                found = true;
                                                break;
                                            }
                                        }

                                        if(!a.letterGrade.equals("N/A") || a.missing)
                                        {
                                            sb.append(a.id + "-" + a.earnedPoints + "\n");
                                            if(!found)
                                            {
                                                newAssignments.add(a);
                                            }
                                        }
                                        category.activities.add(a);
                                    }
                                }

                                category.sort();

                                t.addCategory(category);
                            }
                        }
                        c.tasks.add(t);
                    }
                }

                courses.add(c);
            }

        }

        //save grades to be compared when looking for new grades
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        bw.write(sb.toString());
        bw.close();

        userInfo.newAssignments = newAssignments;

        userInfo.courses = new ArrayList<Course>( );

        //alphabetically sort course list
        for (int i=0;i < courses.size();++i)
        {
            if (userInfo.courses.size() == 0)
            {
                userInfo.courses.add( courses.get(i) );
            }
            else
            {
                int placement = -1;

                for (int j=0;j < userInfo.courses.size();++j)
                {
                    if (courses.get(i).getCourseName().compareTo(userInfo.courses.get(j).getCourseName()) < 0 && placement == -1)
                    {
                        placement = j;
                        break;
                    }
                }
                if (placement != -1)
                    userInfo.courses.add( placement, courses.get(i) );
                else
                    userInfo.courses.add( courses.get(i) );
            }
        }
		
		isLoggedIn = true;
        _districtCode = districtCode;
        _username = username;
        _password = password;
        System.out.println("Logged in and got data!");
		return true;
	}

    public static boolean relogin()
    {
        if (!isLoggedIn)
            return false;

        try
        {
            return login(_districtCode, _username, _password, context);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

	public static void printDebugInfo()
	{
		System.out.println("District Information:");
		System.out.println("District: " + districtInfo.getDistrictName());
		System.out.println("State: " + districtInfo.getDistrictStateCode());
		System.out.println("Base URL: " + districtInfo.getDistrictBaseURL());
		System.out.println("District App Name: " + districtInfo.getDistrictAppName());
	
		if (isLoggedIn( ))
		{
			System.out.println(userInfo.getInfoString());
		}
		else
		{
			System.out.println("\nNot logged in");
		}
	}
}