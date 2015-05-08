package us.plxhack.InfiniteCampus.api.course;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import nu.xom.Element;
import nu.xom.Elements;

public class Course
{
    private int courseNumber;
    private String courseName;
    private Teacher teacher;
    private float percentage;
    private String letterGrade;

    //public ArrayList<Category> gradeCategories;
    public List<Task> tasks;

    public Course(Element e)
    {
        courseNumber = Integer.valueOf( e.getAttributeValue("courseNumber") );
        courseName = formatCourseName(e.getAttributeValue("courseName"));
        System.out.println("Course Name " + courseName);
        teacher = new Teacher( e.getAttributeValue("teacherDisplay") );

        Elements taskChildren = e.getFirstChildElement("tasks").getChildElements();
        ArrayList<Element> stasks = new ArrayList<>();

        for (int j=0;j < taskChildren.size(); j++)
        {
            Element t = taskChildren.get(j);

            if(t != null)
            {
                if (taskChildren.get(j).getAttributeValue("name").equalsIgnoreCase("final"))
                {
                    percentage = Float.valueOf( t.getAttributeValue("percentage") );
                    letterGrade = t.getAttributeValue("letterGrade");
                }
                else if (taskChildren.get(j).getChildCount() != 0 && taskChildren.get(j).getAttributeValue("termName").equalsIgnoreCase( e.getAttributeValue("current_term") ))
                {
                    stasks.add( t );
                }
            }
        }

        tasks = new ArrayList<>();
        for (int l=0;l < stasks.size();++l)
        {
            Element finalTaskGroups = stasks.get(l).getFirstChildElement("groups");

            if(finalTaskGroups != null)
            {
                tasks.add(new Task(stasks.get(l), finalTaskGroups, courseName));
            }
        }

    }

    public Course(JSONObject jsonCourse) throws JSONException
    {
        JSONObject jsonTeacher = jsonCourse.getJSONObject("teacher");
        teacher = new Teacher(jsonTeacher.getString("firstName"), jsonTeacher.getString("lastName"));
        courseNumber = jsonCourse.getInt("number");
        courseName = formatCourseName(jsonCourse.getString("name"));

        percentage = (float)jsonCourse.getDouble("percent");
        letterGrade = jsonCourse.getString("letterGrade");

        JSONArray jsonTasks = jsonCourse.getJSONArray("tasks");
        tasks = new ArrayList<>();
        for(int j = 0; j < jsonTasks.length(); j++)
        {
            tasks.add(new Task(jsonTasks.getJSONObject(j)));
        }
    }

    public Course( int courseNumber, String courseName, String commaSeparatedTeacherName )
    {
        this.courseNumber = courseNumber;
        this.courseName = formatCourseName( courseName );

        teacher = new Teacher( commaSeparatedTeacherName );
        tasks = new ArrayList<Task>();
    }

    public Course( int courseNumber, String courseName, Teacher teacher )
    {
        this.courseNumber = courseNumber;
        this.courseName = formatCourseName( courseName );
        this.teacher = teacher;

        tasks = new ArrayList<Task>();
    }

    private static String formatCourseName( String className )
    {
        String ret = "";

        for (int i=0;i < className.length();++i)
        {
            if (i > 0
                    && className.charAt(i) >= 65 //between capital A
                    && className.charAt(i) <= 90 //and capital Z
                    && !(className.charAt(i-1) >= 32 && className.charAt(i-1) <= 47) //uppercase if the previous character is not a special character or a space
                    && !(className.charAt(i-1) == 'A' && className.charAt(i) == 'P' && (i == className.length() || (className.charAt(i+1) >= 32 && className.charAt(i+1) <= 47))) ) //we want uppercase for AP followed by space/special
            {
                ret += (char)(className.charAt(i)+32);
            }
            else
            {
                ret += className.charAt(i);
            }
        }

        return ret;
    }

    public int getCourseNumber()
    {
        return courseNumber;
    }

    public String getCourseName()
    {
        return courseName;
    }

    public Float getPercent()
    {
        return percentage;
    }

    public Teacher getTeacher()
    {
        return teacher;
    }

    public String getLetterGrade() { return letterGrade; }

    public String getTeacherName()
    {
        return teacher.getFirstName() + " " + teacher.getLastName();
    }
}