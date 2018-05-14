package algorithm;

import java.beans.IntrospectionException;
import java.util.HashMap;
import java.util.Map;

//public class Solution {
//    public int[] twoSum(int[] nums, int target) {
//        int len = nums.length;
//        Map<Integer,Integer> map = new HashMap<Integer,Integer>();
//        for (int i = 0; i<len; i++){
//            int another = target - nums[i];
//            if (map.containsKey(another)){
//                return new int[]{map.get(another),i};
//            }
//            map.put(nums[i],i);
//        }
////        int[] tar;
////
////        for (int i = 0;i  < len; i++){
////            for (int j = i+1;j<len;j++){
////                if(nums[i] + nums[j] ==  target){
////                    return new int[] {i,j};
////                }
////            }
////        }
//        throw new IllegalArgumentException("");
//    }
//    public static void main(String args[]){
//        int num[] = {2, 7, 11, 6,3,15};
//        Long start = System.currentTimeMillis();
//        int re[] = new Solution().twoSum(num,9);
//        Long end = System.currentTimeMillis();
//        System.out.println(end);
//        for (int i = 0; i < re.length; i++){
//            System.out.println(re[i]);
//        }
//
//    }
//
//}



public class Solution {
    public static class ListNode {
        int val;
        ListNode next;
        ListNode(int x) {
            val = x;
        }
    }
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        ListNode dummyHead = new ListNode(0);
        ListNode p = l1, q = l2, curr = dummyHead;
        int carry = 0;
        while (p != null || q != null) {
            int x = (p != null) ? p.val : 0;
            int y = (q != null) ? q.val : 0;
            int sum = carry + x + y;
            carry = sum / 10;
            curr.next = new ListNode(sum % 10);
            curr = curr.next;
            if (p != null) p = p.next;
            if (q != null) q = q.next;
        }
        if (carry > 0) {
            curr.next = new ListNode(carry);
        }
        return dummyHead.next;
    }

    public static void main(String[] args){
        ListNode L1 = new ListNode(10);
        ListNode L2 = new ListNode(5);
        System.out.println(new Solution().addTwoNumbers(L1,L2).val);
    }
}
