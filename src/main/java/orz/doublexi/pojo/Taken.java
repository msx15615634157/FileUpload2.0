package orz.doublexi.pojo;

/**
 * @author ：mengshx
 * @date ：Created in 2020/9/10 9:14
 * @description：令牌,有验证和倒计时等功能
 */
public class Taken {
    //一天后过期
    private volatile int restTime=1000*60*60*24;
    //干脆直接uuid了
    private String key;
    //上次访问的路径
    private String path;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Taken(String key){
        this.key=key;
    }

    public int getRestTime() {
        return restTime;
    }
    public String getKey() {
        return key;
    }

    public void resetRestTime() {
        this.restTime = 1000*60*60*24;
    }
    //若返回false代表该回收了
    public boolean timeReduceOneSecond(){
        if (restTime <= 0) {
            return false;
        }
        restTime=restTime-1000;
        return  true;
    }


}
