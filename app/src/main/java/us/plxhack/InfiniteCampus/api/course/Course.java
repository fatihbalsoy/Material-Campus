package us.plxhack.InfiniteCampus.api.course;

import java.util.ArrayList;

public class Course
{
    private int courseNumber;
    private String courseName;
    private Teacher teacher;

    public float percentage;
    public String letterGrade;

    //public ArrayList<Category> gradeCategories;
    public ArrayList<Task> tasks;

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

    public Course( int courseNumber, String courseName, String commaSeparatedTeacherName )
    {
        this.courseNumber = courseNumber;
        this.courseName = formatCourseName( courseName );

        teacher = new Teacher( commaSeparatedTeacherName );
        tasks = new ArrayList<Task>();
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

    public String getTeacherName()
    {
        return teacher.getFirstName() + " " + teacher.getLastName();
    }
}