import org.jetbrains.annotations.NotNull;
import org.junit.Test;

/**
 * projectName: GmallTwicePrastice
 * 吾常终日而思矣 不如须臾之所学!
 *
 * @author: Clarence
 * time: 2023/1/12 2:43 周四
 * description:
 */

public class test {

    public int[] runningSum(int @NotNull [] nums) {
        int[] arrary = new int[nums.length];
        int sum = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j <= i; j++) {
                sum = sum + nums[j];
            }
            arrary[i] = sum;
            System.out.println(sum);
            sum = 0;
        }
        return arrary;
    }

    @Test
    public void array() {
        int[] ints = {1, 2, 3, 4};
        int[] ints1 = this.runningSum(ints);
        System.out.println(ints1+"niha");
    }/**/
    /**《我亦无他，唯手熟耳!》
         * @Date 2023/1/12 3:02
         * @param  请求参数(请补充)
         * @return 返回结果(请补充)
         * @description :方法二
         */



}
