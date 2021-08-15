//动态查询供货商名
function selectProvider() {
    var providerName = $("#providerName").val();
    $.ajax({
        url : "/pages/inport/selectProviderName",
        type : "post",
        data : {"providerName" : providerName},
        dataType : "json",
        success : function (data) {
            var ulProviderName = $("#ulProviderName");
            ulProviderName.find("li").remove();
            var li = "";
            if (data != null || data != "") {
                $.each(data, function(i,item){
                    li += "<li onclick='replace(this.innerHTML,this.parentNode.id)' >" + item + "</li>";
                });
                ulProviderName.append(li);
            }

        },
        error : function () {
            alert("!!!")
        }

    })
}

//动态模糊查询商品名
function selectGoods() {
    var goodsName = $("#goodsName").val();
    $.ajax({
        url : "/pages/inport/selectGoodsName",
        type : "post",
        data : {"goodsName" : goodsName},
        dataType : "json",
        success : function (data) {
            var ulGoodsName = $("#ulGoodsName");
            ulGoodsName.find("li").remove();
            var li = "";
            if (data != null || data != "") {
                $.each(data, function(i,item){
                    li += "<li onclick='replace(this.innerHTML,this.parentNode.id)' >" + item + "</li>";
                });
                ulGoodsName.append(li);
            }

        },
    })
}

//查询商品size
function selectGoodsSize() {
    var goodsName = $("#goodsName").val();
    $.ajax({
        url : "/pages/inport/selectGoodsSize",
        type : "post",
        data : {"goodsName" : goodsName},
        dataType : "json",
        success : function (data) {
            var ulGoodsSize = $("#ulGoodsSize");
            ulGoodsSize.find("li").remove();
            var li = "";
            if (data != null || data != "") {
                $.each(data, function(i,item){
                    li += "<li onclick='replace(this.innerHTML,this.parentNode.id)' >" + item + "</li>";
                });
                ulGoodsSize.append(li);
            }

        },
        error : function () {
            alert("!!!")
        }

    })
}



function replace(name,id) {
    if ("ulProviderName" === id){
        $("#providerName").val(name);
        $("#ulProviderName").find("li").remove();
    }
    if ("ulGoodsName"=== id) {
        $("#goodsName").val(name);
        $("#ulGoodsName").find("li").remove();
    }
    if ("ulGoodsSize" === id){
        $("#goodsSize").val(name);
        $("#ulGoodsSize").find("li").remove();
    }
}