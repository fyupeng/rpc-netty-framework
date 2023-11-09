import java.io.Serializable;

/**
 * @Auther: fyp
 * @Date: 2022/12/6
 * @Description:
 * @Package: cn.fyupeng.serializer
 * @Version: 1.0
 */
public class Student implements Serializable {

    private int id;
    private String name;
    private boolean sex;

    public Student(int id, String name, boolean sex) {
        this.id = id;
        this.name = name;
        this.sex = sex;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sex=" + sex +
                '}';
    }
}
