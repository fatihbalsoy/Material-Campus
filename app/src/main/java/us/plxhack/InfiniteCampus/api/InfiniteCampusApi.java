package us.plxhack.InfiniteCampus.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.Buffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.ParsingException;
import us.plxhack.InfiniteCampus.api.calendar.Calendar;
import us.plxhack.InfiniteCampus.api.calendar.ScheduleStructure;
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

    public static void loadData(File existing, File saveFile) throws JSONException, IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(saveFile));
        String line = "";
        while((line = br.readLine()) != null)
        {
            sb.append(line);
        }

        JSONObject json = new JSONObject(sb.toString());

        userInfo = new Student(json.getString("firstName"));
        userInfo.middleName = json.getString("middleName");
        userInfo.lastName = json.getString("lastName");
        userInfo.personID = json.getString("id");
        userInfo.studentNumber = json.getString("number");
        userInfo.hasSecurityRole = json.getBoolean("security");
        userInfo.isGuardian = json.getString("guardian");
        _username = json.getString("username");
        _password = json.getString("password");
        _districtCode = json.getString("districtCode");

        JSONArray jsonCourses = json.getJSONArray("courses");
        List<Course> courses = new ArrayList<>();
        for(int i = 0; i < jsonCourses.length(); i++)
        {
            JSONObject jsonCourse = jsonCourses.getJSONObject(i);
            Course course = new Course(jsonCourse.getInt("number"), jsonCourse.getString("name"), jsonCourse.getString("teacher"));

            course.percentage = (float)jsonCourse.getDouble("percent");
            course.letterGrade = jsonCourse.getString("letterGrade");

            JSONArray jsonTasks = jsonCourse.getJSONArray("tasks");
            List<Task> tasks = new ArrayList<>();
            for(int j = 0; j < jsonTasks.length(); j++)
            {
                JSONObject jsonTask = jsonTasks.getJSONObject(j);
                Task task = new Task(jsonTask.getString("name"));

                task.termName = jsonTask.getString("termName");
                task.percentage = (float)jsonTask.getDouble("percent");
                task.earnedPoints = jsonTask.getInt("earnedPoints");
                task.totalPoints = jsonTask.getInt("totalPoints");
                task.weight = jsonTask.getInt("weight");
                task.letterGrade = jsonTask.getString("letterGrade");

                JSONArray jsonCategories = jsonTask.getJSONArray("categories");
                List<Category> categories = new ArrayList<>();
                for(int k = 0; k < jsonCategories.length(); k++)
                {
                    JSONObject jsonCategory = jsonCategories.getJSONObject(k);
                    Category category = new Category(jsonCategory.getString("name"));

                    category.percentage = (float)jsonCategory.getDouble("percent");
                    category.earnedPoints = jsonCategory.getInt("earnedPoints");
                    category.totalPoints = jsonCategory.getInt("totalPoints");
                    category.weight = jsonCategory.getInt("weight");
                    category.letterGrade = jsonCategory.getString("letterGrade");

                    JSONArray jsonActivities = jsonCategory.getJSONArray("activities");
                    List<Activity> activities = new ArrayList<>();
                    for(int l = 0; l < jsonActivities.length(); l++)
                    {
                        JSONObject jsonActivity = jsonActivities.getJSONObject(l);
                        Activity activity = new Activity(jsonActivity.getString("name"));

                        activity.percentage = (float)jsonActivity.getDouble("percent");
                        activity.earnedPoints = jsonActivity.getString("earnedPoints");
                        activity.totalPoints = jsonActivity.getInt("totalPoints");
                        activity.letterGrade = jsonActivity.getString("letterGrade");
                        activity.id = jsonActivity.getString("id");
                        activity.missing = jsonActivity.getBoolean("missing");
                        activity.dueDate = jsonActivity.getString("dueDate");
                        activity.className = jsonActivity.getString("className");

                        activities.add(activity);
                    }
                    category.activities = activities;

                    categories.add(category);
                }
                task.gradeCategories = categories;

                tasks.add(task);
            }
            course.tasks = tasks;

            courses.add(course);
        }
        userInfo.courses = courses;

        JSONArray jsonCalendars = json.getJSONArray("calendars");
        List<Calendar> calendars = new ArrayList<>();
        for(int i = 0; i < jsonCalendars.length(); i++)
        {
            JSONObject jsonCalendar = jsonCalendars.getJSONObject(i);
            Calendar calendar = new Calendar(jsonCalendar.getString("name"));

            calendar.calendarID = jsonCalendar.getString("id");
            calendar.endYear = jsonCalendar.getString("endYear");
            calendar.schoolID = jsonCalendar.getString("schoolId");

            JSONArray jsonSchedules = jsonCalendar.getJSONArray("schedules");
            List<ScheduleStructure> schedules = new ArrayList<>();
            for(int j = 0; j < jsonSchedules.length(); j++)
            {
                JSONObject jsonSchedule = jsonSchedules.getJSONObject(j);
                ScheduleStructure schedule = new ScheduleStructure(jsonSchedule.getString("name"));

                schedule.active = jsonSchedule.getBoolean("active");
                schedule.grade = jsonSchedule.getString("grade");
                schedule.id = jsonSchedule.getString("id");
                schedule.is_default = jsonSchedule.getBoolean("default");
                schedule.label = jsonSchedule.getString("label");
                schedule.primary = jsonSchedule.getString("primary");
                try
                {
                    schedule.startDate = new SimpleDateFormat("MM/dd/yy", Locale.ENGLISH).parse(jsonSchedule.getString("startDate"));
                } catch (ParseException e)
                {
                    e.printStackTrace();
                }

                schedules.add(schedule);
            }
            calendar.schedules = schedules;

            calendars.add(calendar);
        }
        userInfo.calendars = calendars;


    }

    public static boolean loadRemoteData(String districtCode, String username, String password, File existing, File saveFile) throws IOException, ParsingException
    {
        //Get District Information from district code
        CoreManager core = new CoreManager(districtCode);
        districtInfo = core.getDistrictInfo();

        List<Activity> newAssignments = new ArrayList<>();
        List<String> existingAssignments = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(existing));
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
        BufferedWriter bw = new BufferedWriter(new FileWriter(existing));
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
        try
        {
            saveData(saveFile);
        } catch (JSONException e)
        {
            e.printStackTrace();
        }
        System.out.println("Logged in and got data!");
        return true;
    }

    public static void saveData(File saveFile) throws JSONException, IOException
    {
        JSONObject json = new JSONObject();

        json.put("firstName", userInfo.firstName);
        json.put("middleName", userInfo.middleName);
        json.put("lastName", userInfo.lastName);
        json.put("id", userInfo.personID);
        json.put("number", userInfo.studentNumber);
        json.put("security", userInfo.hasSecurityRole);
        json.put("guardian", userInfo.isGuardian);
        json.put("password", _password);
        json.put("username", _username);
        json.put("districtCode", _districtCode);

        JSONArray jsonCourses = new JSONArray();
        List<Course> courses = userInfo.courses;
        for(int i = 0; i < courses.size(); i++)
        {
            JSONObject jsonCourse = new JSONObject();
            Course course = courses.get(i);

            jsonCourse.put("name", course.getCourseName());
            jsonCourse.put("teacher", course.getTeacherName());
            jsonCourse.put("percent", course.getPercent());
            jsonCourse.put("letterGrade", course.getLetterGrade());
            jsonCourse.put("number", course.getCourseNumber());

            JSONArray jsonTasks = new JSONArray();
            List<Task> tasks = course.tasks;
            for(int j = 0; j < tasks.size(); j++)
            {
                JSONObject jsonTask = new JSONObject();
                Task task = tasks.get(j);

                jsonTask.put("name", task.name);
                jsonTask.put("termName", task.termName);
                jsonTask.put("percent", task.percentage);
                jsonTask.put("earnedPoints", task.earnedPoints);
                jsonTask.put("totalPoints", task.totalPoints);
                jsonTask.put("weight", task.weight);
                jsonTask.put("letterGrade", task.letterGrade);

                JSONArray jsonCategories = new JSONArray();
                List<Category> categories = task.gradeCategories;
                for(int k = 0; k < categories.size(); k++)
                {
                    JSONObject jsonCategory = new JSONObject();
                    Category category = categories.get(k);

                    jsonCategory.put("name", category.name);
                    jsonCategory.put("percent", category.percentage);
                    jsonCategory.put("earnedPoints", category.earnedPoints);
                    jsonCategory.put("totalPoints", category.totalPoints);
                    jsonCategory.put("weight", category.weight);
                    jsonCategory.put("letterGrade", category.letterGrade);

                    JSONArray jsonActivities = new JSONArray();
                    List<Activity> activities = category.activities;
                    for(int l = 0; l < activities.size(); l++)
                    {
                        JSONObject jsonActivity = new JSONObject();
                        Activity activity = activities.get(l);

                        jsonActivity.put("name", activity.name);
                        jsonActivity.put("percent", activity.percentage);
                        if(activity.earnedPoints == null)
                        {
                            jsonActivity.put("earnedPoints", "");
                        }
                        else
                        {
                            jsonActivity.put("earnedPoints", activity.earnedPoints);
                        }
                        jsonActivity.put("totalPoints", activity.totalPoints);
                        jsonActivity.put("id", activity.id);
                        jsonActivity.put("letterGrade", activity.letterGrade);
                        jsonActivity.put("missing", activity.missing);
                        jsonActivity.put("dueDate", activity.dueDate);
                        jsonActivity.put("className", activity.className);

                        jsonActivities.put(jsonActivity);
                    }
                    jsonCategory.put("activities", jsonActivities);

                    jsonCategories.put(jsonCategory);
                }
                jsonTask.put("categories", jsonCategories);

                jsonTasks.put(jsonTask);
            }
            jsonCourse.put("tasks", jsonTasks);

            jsonCourses.put(jsonCourse);
        }
        json.put("courses", jsonCourses);

        JSONArray jsonCalendars = new JSONArray();
        List<Calendar> calendars = userInfo.calendars;
        for(int i = 0; i < calendars.size(); i++)
        {
            JSONObject jsonCalendar = new JSONObject();
            Calendar calendar = calendars.get(i);

            jsonCalendar.put("id", calendar.calendarID);
            jsonCalendar.put("endYear", calendar.endYear);
            jsonCalendar.put("name", calendar.name);
            jsonCalendar.put("schoolId", calendar.schoolID);

            JSONArray jsonSchedules = new JSONArray();
            List<ScheduleStructure> schedules = calendar.schedules;
            for(int j = 0; j < schedules.size(); j++)
            {
                JSONObject jsonSchedule = new JSONObject();
                ScheduleStructure schedule = schedules.get(j);

                jsonSchedule.put("active", schedule.active);
                jsonSchedule.put("grade", schedule.grade);
                jsonSchedule.put("id", schedule.id);
                jsonSchedule.put("default", schedule.is_default);
                jsonSchedule.put("label", schedule.label);
                jsonSchedule.put("name", schedule.name);
                jsonSchedule.put("primary", schedule.primary);
                jsonSchedule.put("startDate", schedule.startDate);

                jsonSchedules.put(jsonSchedule);
            }
            jsonCalendar.put("schedules", jsonSchedules);

            jsonCalendars.put(jsonCalendar);
        }

        json.put("calendars", jsonCalendars);

        System.out.println("JSON created... writing to file");
        String jsonString = json.toString();
        BufferedWriter bw = new BufferedWriter(new FileWriter(saveFile));
        bw.write(jsonString);
        bw.close();
    }

	public static boolean isLoggedIn( )
	{
		return isLoggedIn;
	}
	
	public static boolean login( String districtCode, String username, String password, Context con , boolean ignoreDontSync) throws ParsingException, IOException
    {
        context = con;

        boolean online;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null)
        {
            online = false;
        }
        else
        {
            online = ni.isConnected();
        }

        File existing = new File(context.getFilesDir(), "existinggrades.data");
        File saveFile = new File(context.getFilesDir(), "save.data");

        SharedPreferences settings = context.getSharedPreferences("MaterialCampus", 0);
        if(online && (!settings.getBoolean("dontSync", false) || ignoreDontSync))
        {
            System.out.println("Loading data from internet");
            if(!existing.exists())
            {
                existing.createNewFile();
            }
            if(!saveFile.exists())
            {
                existing.createNewFile();
            }
            return loadRemoteData(districtCode, username, password, existing, saveFile);
        }
        else if(saveFile.exists())
        {
            System.out.println("No internet connect but save file exists");
            try
            {
                loadData(existing, saveFile);
            } catch (JSONException e)
            {
                e.printStackTrace();
                return false;
            }
            System.out.println("Finished loading data");
        }
        else
        {
            System.out.println("Please connect to the internet");
            return false;
        }

		return true;
	}

    public static boolean relogin()
    {
        System.out.println("Relogging in");
        if (_username == null || _password == null || _districtCode == null)
            return false;

        try
        {
            return login(_districtCode, _username, _password, context, true);
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
