package us.plxhack.InfiniteCampus.api.course;

public class Activity
{
    public String name;

    public float percentage;
    public float totalPoints;
    public String earnedPoints;
    public String letterGrade;
    public boolean missing;

    public String dueDate;

    public Activity( String name )
    {
        this.name = name;
    }
}
