package alexclin.widget.recyclerview.demo;

import alexclin.widget.recyclerview.FlowLayoutManager2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alexclin on 16/2/26.
 */
public class Item {
    public static final int[] COLORS = {R.color.white,R.color.black,R.color.blue,R.color.green,R.color.red};
    private int width;
    private int height;
    private int type;

    public Item(int type,int width, int height) {
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getTextColor(){
        int index = (type+1)%5;
        return COLORS[index];
    }

    public int getBackgroundColor(){
        return COLORS[type%5];
    }

    public static List<Item> createList(int orientation){
        List<Item> items = new ArrayList<>();
        if(orientation== FlowLayoutManager2.VERTICAL) {
            items.add(new Item(0, 12, 6));
            items.add(new Item(1, 6, 3));
            items.add(new Item(2, 6, 6));
            items.add(new Item(4, 6, 6));
            items.add(new Item(5, 3, 3));
            items.add(new Item(4, 3, 3));
            //1 -6
            items.add(new Item(0, 6, 3));
            items.add(new Item(1, 6, 2));
            items.add(new Item(2, 3, 4));
            items.add(new Item(4, 3, 3));
            items.add(new Item(1, 6, 3));
            items.add(new Item(6, 3, 4));
            items.add(new Item(7, 2, 3));
            items.add(new Item(8, 3, 2));
            items.add(new Item(9, 2, 8));
            items.add(new Item(10, 2, 6));
            items.add(new Item(11, 3, 6));
            items.add(new Item(3, 2, 5));
            items.add(new Item(14, 3, 5));
            items.add(new Item(3, 2, 2));
            //2
            items.add(new Item(0, 6, 3));
            items.add(new Item(1, 6, 2));
            items.add(new Item(2, 3, 4));
            items.add(new Item(4, 3, 3));
            items.add(new Item(1, 6, 3));
            items.add(new Item(6, 3, 4));
            items.add(new Item(7, 2, 3));
            items.add(new Item(8, 3, 2));
            items.add(new Item(9, 2, 8));
            items.add(new Item(10, 2, 6));
            items.add(new Item(11, 3, 6));
            items.add(new Item(3, 2, 5));
            items.add(new Item(14, 3, 5));
            items.add(new Item(3, 2, 2));
        }else{
            items.add(new Item(0,6,12));
            items.add(new Item(1,6,3));
            items.add(new Item(2,4,5));
            items.add(new Item(3,8,4));
            items.add(new Item(4,8,5));
            items.add(new Item(0,6,3));
            items.add(new Item(1,4,4));
            //1
            items.add(new Item(2,4,3));
            items.add(new Item(3,4,4));
            items.add(new Item(0,8,5));
            items.add(new Item(0,4,4));
            items.add(new Item(6,8,3));
            items.add(new Item(3,4,4));
            items.add(new Item(2,4,5));

            items.add(new Item(0,6,12));
            items.add(new Item(1,6,3));
            items.add(new Item(2,4,5));
            items.add(new Item(3,8,4));
            items.add(new Item(4,8,5));
            items.add(new Item(0,6,3));
            items.add(new Item(1,4,4));
            //2
            items.add(new Item(2,4,3));
            items.add(new Item(3,4,4));
            items.add(new Item(0,8,5));
            items.add(new Item(0,4,4));
            items.add(new Item(6,8,3));
            items.add(new Item(3,4,4));
            items.add(new Item(2,4,5));
        }
        return items;
    }

    public static List<Item> paddingList() {
        List<Item> items = new ArrayList<>();
        items.add(new Item(0, 12, 6));
        items.add(new Item(1, 6, 3));
        items.add(new Item(2, 6, 6));
        items.add(new Item(4, 6, 6));
        items.add(new Item(5, 3, 3));

        items.add(new Item(4, 3, 3));
        //1
        items.add(new Item(0, 6, 3));
        items.add(new Item(1, 6, 2));
        items.add(new Item(2, 3, 4));
        items.add(new Item(4, 3, 4));

        items.add(new Item(1, 6, 3));
        items.add(new Item(2, 6, 6));
        items.add(new Item(4, 6, 6));
        items.add(new Item(5, 3, 3));
        items.add(new Item(4, 3, 3));

        items.add(new Item(0, 6, 3));
        return items;
    }
}
