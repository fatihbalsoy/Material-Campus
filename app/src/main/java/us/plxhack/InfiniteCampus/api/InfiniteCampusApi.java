package us.plxhack.InfiniteCampus.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fruko.materialcampus.MCActivity;

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
import java.util.ArrayList;
import java.util.List;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import us.plxhack.InfiniteCampus.api.calendar.Calendar;
import us.plxhack.InfiniteCampus.api.calendar.ScheduleStructure;
import us.plxhack.InfiniteCampus.api.course.Activity;
import us.plxhack.InfiniteCampus.api.course.Category;
import us.plxhack.InfiniteCampus.api.course.Task;
import us.plxhack.InfiniteCampus.api.course.Teacher;
import us.plxhack.InfiniteCampus.api.district.DistrictInfo;
import us.plxhack.InfiniteCampus.api.course.Course;

public class InfiniteCampusApi
{
    private static InfiniteCampusApi instance;
	private DistrictInfo districtInfo;
	private Student userInfo;
	private boolean isLoggedIn = false;
    private File saveFile;
    private List<Activity> updated;

    private String _districtCode, _username, _password;

    private Context context;

    public static InfiniteCampusApi getInstance()
    {
        if(instance == null)
        {
            instance = new InfiniteCampusApi();
        }
        return instance;
    }

	private URL getFormattedURL( String document )
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
	
	private String getFormattedURLString( String document )
	{
		return districtInfo.getDistrictBaseURL() + document;
	}

    public void loadData(File saveFile) throws JSONException, IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(saveFile));
        String line = "";
        while((line = br.readLine()) != null)
        {
            sb.append(line);
        }

        JSONObject json = new JSONObject(sb.toString());

        userInfo = new Student(json);

        _username = json.getString("username");
        _password = json.getString("password");
        _districtCode = json.getString("districtCode");

    }

    public boolean loadRemoteData(String districtCode, String username, String password, File saveFile) throws IOException, ParsingException
    {
        //Get District Information from district code
        CoreManager core = new CoreManager(districtCode);
        districtInfo = core.getDistrictInfo();

        updated = new ArrayList<>();

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
        for(int i = 0; i < userInfo.getCalendars().size(); i++)
        {
            URL infoURL2 = getFormattedURL("prism?&x=portal.PortalClassbook-getClassbookForAllSections&mode=classbook&personID=" + userInfo.getPersonID() + "&structureID=" + userInfo.getCalendars().get(i).schedules.get(0).getId() + "&calendarID=" + userInfo.getCalendars().get(i).getCalendarID());
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
        ArrayList<Course> courses = new ArrayList<>();

        for (int i=0;i < courseElements.size();++i)
        {
            Element e = courseElements.get(i);

            if(e != null)
            {
                courses.add(new Course(e));
            }

        }

        userInfo.updateCourses(courses, updated);

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

    public void saveData(File saveFile) throws JSONException, IOException
    {
        JSONObject json = new JSONObject();

        json.put("firstName", userInfo.getFirstName());
        json.put("middleName", userInfo.getMiddleName());
        json.put("lastName", userInfo.getLastName());
        json.put("id", userInfo.getPersonID());
        json.put("number", userInfo.getStudentNumber());
        json.put("security", userInfo.isInSecurityRole());
        json.put("guardian", userInfo.isGuardian());
        json.put("password", _password);
        json.put("username", _username);
        json.put("districtCode", _districtCode);

        JSONArray jsonCourses = new JSONArray();
        List<Course> courses = userInfo.getCourses();
        for(int i = 0; i < courses.size(); i++)
        {
            JSONObject jsonCourse = new JSONObject();
            Course course = courses.get(i);

            jsonCourse.put("name", course.getCourseName());
            jsonCourse.put("percent", course.getPercent());
            jsonCourse.put("letterGrade", course.getLetterGrade());
            jsonCourse.put("number", course.getCourseNumber());

            JSONArray jsonTasks = new JSONArray();
            List<Task> tasks = course.tasks;
            for(int j = 0; j < tasks.size(); j++)
            {
                JSONObject jsonTask = new JSONObject();
                Task task = tasks.get(j);

                jsonTask.put("name", task.getName());
                jsonTask.put("termName", task.getTermName());
                jsonTask.put("percent", task.getPercentage());
                jsonTask.put("earnedPoints", task.getEarnedPoints());
                jsonTask.put("totalPoints", task.getTotalPoints());
                jsonTask.put("weight", task.getWeight());
                if(task.getLetterGrade() == null)
                {
                    jsonTask.put("letterGrade", "N/A");
                }
                else
                {
                    jsonTask.put("letterGrade", task.getLetterGrade());
                }

                JSONArray jsonCategories = new JSONArray();
                List<Category> categories = task.getCategories();
                for(int k = 0; k < categories.size(); k++)
                {
                    JSONObject jsonCategory = new JSONObject();
                    Category category = categories.get(k);

                    jsonCategory.put("name", category.getName());
                    jsonCategory.put("percent", category.getPercentage());
                    jsonCategory.put("earnedPoints", category.getEarnedPoints());
                    jsonCategory.put("totalPoints", category.getTotalPoints());
                    jsonCategory.put("weight", category.getWeight());
                    if(category.getLetterGrade() == null)
                    {
                        jsonCategory.put("letterGrade", "N/A");
                    }
                    else
                    {
                        jsonCategory.put("letterGrade", category.getLetterGrade());
                    }

                    JSONArray jsonActivities = new JSONArray();
                    List<Activity> activities = category.getActivites();
                    for(int l = 0; l < activities.size(); l++)
                    {
                        JSONObject jsonActivity = new JSONObject();
                        Activity activity = activities.get(l);

                        jsonActivity.put("name", activity.getName());
                        jsonActivity.put("percent", activity.getPercentage());
                        if(activity.getEarnedPoints() == null)
                        {
                            jsonActivity.put("earnedPoints", "");
                        }
                        else
                        {
                            jsonActivity.put("earnedPoints", activity.getEarnedPoints());
                        }
                        jsonActivity.put("totalPoints", activity.getTotalPoints());
                        jsonActivity.put("id", activity.getId());
                        jsonActivity.put("letterGrade", activity.getLetterGrade());
                        jsonActivity.put("missing", activity.isMissing());
                        jsonActivity.put("dueDate", activity.getDueDate());
                        jsonActivity.put("className", activity.getClassName());
                        jsonActivity.put("existing", activity.isExisting());

                        jsonActivities.put(jsonActivity);
                    }
                    jsonCategory.put("activities", jsonActivities);

                    jsonCategories.put(jsonCategory);
                }
                jsonTask.put("categories", jsonCategories);

                jsonTasks.put(jsonTask);
            }
            jsonCourse.put("tasks", jsonTasks);

            JSONObject jsonTeacher = new JSONObject();
            Teacher teacher = course.getTeacher();
            jsonTeacher.put("firstName", teacher.getFirstName());
            jsonTeacher.put("lastName", teacher.getLastName());
            jsonCourse.put("teacher", jsonTeacher);

            jsonCourses.put(jsonCourse);
        }
        json.put("courses", jsonCourses);

        JSONArray jsonCalendars = new JSONArray();
        List<Calendar> calendars = userInfo.getCalendars();
        for(int i = 0; i < calendars.size(); i++)
        {
            JSONObject jsonCalendar = new JSONObject();
            Calendar calendar = calendars.get(i);

            jsonCalendar.put("id", calendar.getCalendarID());
            jsonCalendar.put("endYear", calendar.getEndYear());
            jsonCalendar.put("name", calendar.getName());
            jsonCalendar.put("schoolId", calendar.getSchoolID());

            JSONArray jsonSchedules = new JSONArray();
            List<ScheduleStructure> schedules = calendar.schedules;
            for(int j = 0; j < schedules.size(); j++)
            {
                JSONObject jsonSchedule = new JSONObject();
                ScheduleStructure schedule = schedules.get(j);

                jsonSchedule.put("active", schedule.isActive());
                jsonSchedule.put("grade", schedule.getGrade());
                jsonSchedule.put("id", schedule.getId());
                jsonSchedule.put("default", schedule.isDefault());
                jsonSchedule.put("label", schedule.getLabel());
                jsonSchedule.put("name", schedule.getName());
                jsonSchedule.put("primary", schedule.getPrimary());
                jsonSchedule.put("startDate", schedule.getStartDate());

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

	public boolean isLoggedIn( )
	{
		return isLoggedIn;
	}
	
	public boolean login( String districtCode, String username, String password, Context con , boolean ignoreDontSync) throws ParsingException, IOException
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

        saveFile = new File(context.getFilesDir(), "save.data");

        SharedPreferences settings = context.getSharedPreferences("MaterialCampus", 0);
        System.out.println("Dont Sync: " + settings.getBoolean("dontSync", false));
        System.out.println("Ignore Dont Sync: " + ignoreDontSync);
        if(online && (!settings.getBoolean("dontSync", false) || ignoreDontSync))
        {
            System.out.println("Loading data from internet");
            if(!saveFile.exists())
            {
                saveFile.createNewFile();
            }
            return loadRemoteData(districtCode, username, password, saveFile);
        }
        else if(saveFile.exists())
        {
            System.out.println("No internet connection but save file exists");
            try
            {
                loadData(saveFile);
            } catch (JSONException e)
            {
                e.printStackTrace();
                if(online)
                {
                    System.out.println("Loading data from internet");
                    if(!saveFile.exists())
                    {
                        saveFile.createNewFile();
                    }
                    return loadRemoteData(districtCode, username, password, saveFile);
                }
                else
                {
                    return false;
                }
            }
            System.out.println("Finished loading data");
        }
        else
        {
            if(online)
            {
                System.out.println("Loading data from internet");
                if(!saveFile.exists())
                {
                    saveFile.createNewFile();
                }
                return loadRemoteData(districtCode, username, password, saveFile);
            }
            else
            {
                System.out.println("Please connect to the internet");
                return false;
            }
        }

		return true;
	}

    public void refresh()
    {
        try
        {
            loadData(saveFile);
        } catch (JSONException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        System.out.println("Finished loading data");
    }

    public boolean relogin()
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

    public List<Activity> searchClass(Course course, String search)
    {
        List<Activity> assignments = new ArrayList<>();
        for(int i = 0; i < course.tasks.size(); i++)
        {
            for(int j = 0; j < course.tasks.get(i).getCategories().size(); j++)
            {
                for(int k = 0; k < course.tasks.get(i).getCategories().get(j).getActivites().size(); k++)
                {
                    Activity activity = course.tasks.get(i).getCategories().get(j).getActivites().get(k);
                    if(activity.getName().toLowerCase().contains(search.toLowerCase()))
                    {
                        assignments.add(activity);
                    }
                }
            }
        }
        return assignments;
    }

    public List<Activity> searchAllClasses(String search)
    {
        List<Activity> results = new ArrayList<>();
        List<Activity> assignments = getAllAssignments();
        for(int i = 0; i < assignments.size(); i++)
        {
            Activity activity = assignments.get(i);
            if(activity.getName().toLowerCase().contains(search.toLowerCase()))
            {
                results.add(activity);
            }
        }
        return results;
    }

    public List<Activity> getAllMissingAssignments()
    {
        List<Activity> results = new ArrayList<>();
        List<Activity> assignments = getAllAssignments();
        for(int i = 0; i < assignments.size(); i++)
        {
            Activity activity = assignments.get(i);
            if(activity.isMissing())
            {
                results.add(activity);
            }
        }
        return results;
    }

	public void printDebugInfo()
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

    public JSONObject findAssignmentJSON(File saveFile, String assignmentId) throws JSONException, IOException
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(saveFile));
        String line = "";
        while((line = br.readLine()) != null)
        {
            sb.append(line);
        }

        if(!sb.toString().equals(""))
        {
            JSONObject json = new JSONObject(sb.toString());

            JSONArray jsonCourses = json.getJSONArray("courses");
            for(int i = 0; i < jsonCourses.length(); i++)
            {
                JSONObject jsonCourse = jsonCourses.getJSONObject(i);
                JSONArray jsonTasks = jsonCourse.getJSONArray("tasks");
                for(int j = 0; j < jsonTasks.length(); j++)
                {
                    JSONObject jsonTask = jsonTasks.getJSONObject(j);
                    JSONArray jsonCategories = jsonTask.getJSONArray("categories");
                    for(int k = 0; k < jsonCategories.length(); k++)
                    {
                        JSONObject jsonCategory = jsonCategories.getJSONObject(k);
                        JSONArray jsonActivities = jsonCategory.getJSONArray("activities");
                        for(int l = 0; l < jsonActivities.length(); l++)
                        {
                            JSONObject jsonActivity = jsonActivities.getJSONObject(l);
                            String id = jsonActivity.getString("id");
                            if(id.equals(assignmentId))
                            {
                                return jsonActivity;
                            }
                        }
                    }
                }
            }
        }
        else
        {
            System.out.println("String is empty... ignoring");
        }
        return null;
    }

    public List<Activity> getAllAssignments()
    {
        List<Activity> assignments = new ArrayList<>();
        for(int l = 0; l < userInfo.getCourses().size(); l++)
        {
            for(int i = 0; i < userInfo.getCourses().get(l).tasks.size(); i++)
            {
                for(int j = 0; j < userInfo.getCourses().get(l).tasks.get(i).getCategories().size(); j++)
                {
                    for(int k = 0; k < userInfo.getCourses().get(l).tasks.get(i).getCategories().get(j).getActivites().size(); k++)
                    {
                        Activity activity = userInfo.getCourses().get(l).tasks.get(i).getCategories().get(j).getActivites().get(k);
                        assignments.add(activity);
                    }
                }
            }
        }
        return assignments;
    }

    public Student getUserInfo()
    {
        return userInfo;
    }

    public File getSaveFile()
    {
        return saveFile;
    }

    public List<Activity> getUpdated()
    {
        return updated;
    }
}
