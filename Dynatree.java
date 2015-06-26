package jp.microad.digitalSignage.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.microad.digitalSignage.constant.CommonConst;
import jp.microad.digitalSignage.model.Node;

public class DynatreeUtil {

    private HashMap<String, Object> tree;

    public DynatreeUtil(List<Node> nodeList) {
        this.tree = convertTreeListToHash(nodeList);
    }

    @SuppressWarnings("unchecked")
    /**
     * ツリーリストをハッシュ(親ノードをキーとして)に変換
     * 子ノードを速めに探す用
     * @param treeList ツリーリスト
     * @return ハッシュ
     */
    private HashMap<String, Object> convertTreeListToHash(List<Node> nodeList) {
        HashMap<String, Object> hash = new HashMap<String, Object>();

        for (Node node : nodeList) {
            String parentNodeId = String.valueOf(node.getParentId());
            List<Node> subNodeList = null;
            if (!hash.containsKey(parentNodeId)) {
                subNodeList = new ArrayList<Node>();
            } else {
                subNodeList = (List<Node>) hash.get(parentNodeId);
            }
            subNodeList.add(node);
            hash.put(parentNodeId, subNodeList);
        }

        return hash;
    }

    /**
     * 親ノードより、子ノードがあるかを判断
     * 
     * @param parentId 親ノード
     * @return 子ノードがあるか
     */
    private boolean hasChild(int parentId) {
        // 最後の階層
        if (getChildList(parentId) == null) {
            return false;
        }
        return getChildList(parentId).size() > 0 ? true : false;
    }

    @SuppressWarnings("unchecked")
    /**
     * 親ノードより、子ノードリストを取得
     * @param parentId 親ノード
     * @return 子ノードリスト
     */
    private List<Node> getChildList(int parentId) {
        return (List<Node>) this.tree.get(String.valueOf(parentId));
    }

    /**
     * 親ノードより、最後の階層までのノードを取得
     * 
     * @param parentId 親ノード
     * @return 親ノードに紐づくすべてのノード
     */
    public List<Map<String, Object>> recurse(int parentId) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        List<Node> items = getChildList(parentId);
        for (Node node : items) {
            Map<String, Object> map = new HashMap<>();
            map.put("key", node.getId());
            map.put("title", node.getTitle());
            map.put("icon", node.isIcon());
            if (hasChild(node.getId())) {
                map.put("children", recurse(node.getId()));
            }
            list.add(map);
        }

        return list;
    }

    /**
     * Dynatree用のストラクトのデータを取得
     * 
     * @return Dynatree用のストラクトのデータ
     */
    public List<Map<String, Object>> getTreeStructData() {
        return recurse("0");
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // Node node1 = new Node(10, 0, "北海道");
        // Node node2 = new Node(1001, 10, "北海道");
        // Node node3 = new Node(10011001, 1001, "札幌市");
        // Node node4 = new Node(10011002, 1001, "函館市");
        // Node node5 = new Node(10011003, 1001, "小樽市");
        // Node node6 = new Node(20, 0, "東北");
        // Node node7 = new Node(30, 0, "関東");
        // Node node8 = new Node(3001, 30, "関東1");
        //
        // List<Node> geoList = new ArrayList<Node>();
        // geoList.add(node1);
        // geoList.add(node2);
        // geoList.add(node3);
        // geoList.add(node4);
        // geoList.add(node5);
        // geoList.add(node6);
        // geoList.add(node7);
        // geoList.add(node8);
        
        // ----------------------------国别，地区逻辑----------------------------------
        // 取得国别
        List<CountryMaster> countryList = new ArrayList<CountryMaster>();
        CountryMaster cm1 = new CountryMaster();
        cm1.setCountryId(1);
        cm1.setCountryName("日本");
        countryList.add(cm1);
        CountryMaster cm2 = new CountryMaster();
        cm2.setCountryId(2);
        cm2.setCountryName("海外");
        countryList.add(cm2);
        
        // 取得国别，地区
        Database db = new Database();
        List<GeoCountryMaster> list = db.selectGeoCountryMaster();

        long s = System.currentTimeMillis();
        
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        for(CountryMaster cm : countryList){
            
            Map<String, Object> map = new HashMap<>();
            map.put("key", cm.getCountryId());
            map.put("title", cm.getCountryName());
            
            // 初始化国别对应的地区数据
            List<Node> geoList = new ArrayList<Node>();
            for (GeoCountryMaster gcm : list) {
                if(gcm.getCountryId() == cm.getCountryId()){
                    Node node = new Node();
                    node.setId(gcm.getGeoId());
                    node.setParentId(gcm.getParentGeoId());
                    node.setTitle(gcm.getGeoName());
                    geoList.add(node);
                }
            }
            Dynatree dynatree = new Dynatree(geoList);
            List<Map<String, Object>> treeStructList = dynatree.getTreeStructList();
            if(treeStructList.size() > 0)
                map.put("children", treeStructList);
            
            resultList.add(map);
        }
        long e = System.currentTimeMillis();
        
        System.out.println(e - s);
        
        String result = JSON.encode(resultList);
        System.out.println(result);
    }
}



package jp.microad.digitalSignage.model;

public class Node {

    private int id;
    private int parentId;
    private String title;
    private boolean icon;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the parentId
     */
    public int getParentId() {
        return parentId;
    }

    /**
     * @param parentId the parentId to set
     */
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the icon
     */
    public boolean isIcon() {
        return icon;
    }

    /**
     * @param icon the icon to set
     */
    public void setIcon(boolean icon) {
        this.icon = icon;
    }

}

//数据结构
country_id  country_name    area_id     area_name   hierarchy
1           USA             20      Texas           /20/
1           USA             2003    Dallas          /20/2003/
1           USA             2004    SA              /20/2004/

var treeData = [
    {title: "USA", key: "1", expand: false,
            children: [
                {title: "Texas", key: "20",
                    children: [
                        {title: "Dallas", key: "2003" },
                        {title: "SA", key: "2004"}
                     ]
                }
            ]
    }
]


//js 结构
function TreeInit() {
    $.ajax({
        type : 'GET',
        url : "async/getNodesAsJson",
        dataType : 'json',
        cache : false,
        success : function(data) {

            $("#tree").dynatree({
                children: data,
                 // 是否支持checkbox
                checkbox: true,
                selectMode: 3,
                onDblClick: function(node, event) {
                    node.toggleSelect();
                },
                onPostInit : function() {
                    // 初期化时，tree不可用
                    $("#tree").dynatree("disable");
                },
                onRender : function(node, nodeSpan) {
                    // 初期化时，节点展开
                    node.expand(true);

                    // 设定selKeys以外的key节点隐藏
                    var selKeys = [1, 2, 3];              
                    if (selKeys && $.inArray(node.data.key, selKeys ) < 0) {
                        $(nodeSpan).parent().css("display", "none"); 

                },
          });

          // 依据key值设定某个节点选中
          $("#tree").dynatree("getTree").selectKey("#key#", true);
        }
    });
  }
}

// 取得选中节点key
function getSelectedKeys() {
    var selKeys = $.map($("#tree").dynatree("getSelectedNodes"), function(node){
    return node.data.key;
});

// tree重新描画，触发onRender()
$("#tree").dynatree("getTree").redraw(); 

// tree不可用
$("#tree").dynatree("disable");

// tree可用
$("#tree").dynatree("enable");

// 取得当前tree状态数据（可用于tree的数据源）
var data = $("#tree").dynatree("getTree").toDict();

// 取得当前tree总节点数
$("#tree").dynatree("getTree").count();

// jsp 结构
<div id="tree" style="width:170px;height:200px;overflow:auto;"></div>


//java js交互

 @RequestMapping(
            value = "/async/getNodesAsJson",
            method = RequestMethod.GET,
            produces = "text/plain;charset=UTF-8")
    public @ResponseBody
    String getNodesAsJson(
            Model model,
            HttpSession session,
            HttpServletRequest request) throws Exception {  
        // 取得db数据
        List<GeographyMaster> geographyCountryList = commonDao.selectGeographyCountry();

        // 转换db数据为树工具类能识别的结构
        List<Node> geoNodeList = new ArrayList<Node>();
        for (GeographyMaster gm : geographyCountryList) {
             Node geoNode = new Node();
             geoNode.setId(gm.getGeoId());
             geoNode.setTitle(gm.getGeoName());
             geoNode.setParentId(gm.getParentGeoId());
             geoNode.setIcon(false);
             geoNodeList.add(geoNode);

        }
        // 取得前台js用树结构对象
        DynatreeUtil dynatree = new DynatreeUtil(geoNodeList);
        List<Map<String, Object>> treeStructDataList = dynatree.getTreeStructData();

        String result = JSON.encode(treeStructDataList);
        return result;
    }
