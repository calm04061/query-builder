package com.calm.entity.processor.util;

import com.calm.entity.processor.Constants;
import com.calm.entity.processor.entity.BaseEntity;
import com.calm.entity.processor.entity.Sortable;
import com.calm.entity.processor.entity.Tree;

import java.util.*;

/**
 * Created by dingqihui on 2017/2/11.
 */
public class EntityUtil {
    public static <I, E extends BaseEntity<I>> Map<I, E> map(List<E> list) {
        Map<I, E> result = new LinkedHashMap<>();
        for (E e : list) {
            I id = e.getId();
            result.put(id, e);
        }
        return result;
    }

    public static <I, E extends Tree<I, E>> List<E> buildTree(List<E> listNodes) {
        Map<I, List<E>> mapTree = new LinkedHashMap<>();

        for (E lessonTemplateNode : listNodes) {
            I parentId = lessonTemplateNode.getParentId();
            List<E> list = mapTree.get(parentId);
            if (list == null) {
                list = new ArrayList<>();
                mapTree.put(parentId, list);
            }
            list.add(lessonTemplateNode);
        }
        List<E> result = new ArrayList<>();
        List<E> top = mapTree.get(Constants.DB_DEFAULT_VALUE);
        if (top != null) {
            for (E node : top) {
                result.add(node);
                buildTreeMap(mapTree, node);
            }
        }
        return result;
    }

    private static <I, E extends Tree<I, E>> void buildTreeMap(Map<I, List<E>> mapTree, E node) {
        List<E> tempList = mapTree.get(node.getId());
        if (tempList != null) {
            if (node instanceof Sortable) {
                List<Sortable> tempSort = new ArrayList<>();
                tempList = mapTree.get(node.getId());
                for (E e : tempList) {
                    tempSort.add((Sortable) e);
                }
                tempList.clear();
                Collections.sort(tempSort, SortableComparator.DEFAULT_INST);
                for (Sortable temp : tempSort) {
                    tempList.add((E) temp);
                }
            }
            node.setChildren(tempList);
            for (E tempNode : tempList) {
                tempNode.setParent(node);
                buildTreeMap(mapTree, tempNode);
            }
        }
    }
}
