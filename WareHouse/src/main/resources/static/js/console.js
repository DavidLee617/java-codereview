"use strict";
layui.use(["okUtils", "table", "countUp"], function () {
    var countUp = layui.countUp;
    var table = layui.table;
    var okUtils = layui.okUtils;
    var $ = layui.jquery;

    function statText1() {
        var elem_nums = $(".sales-num");
        elem_nums.each(function (i, j) {
            $.ajax({
                type: "Post",
                url: "/getsalesnumSum",
                dataType: "json",
                success: function(data){
                    $("#sales-num1").html(data)
                },
                error: function(err) {
                    alert(err);
                }
            });
        });
    }
    function statText2() {
        var elem_nums = $(".goods-num");
        elem_nums.each(function (i, j) {
            $.ajax({
                type: "Post",
                url: "/getgoodsCount",
                dataType: "json",
                success: function(data){
                    $("#goods-num2").html(data)
                },
                error: function(err) {
                    alert(err);
                }
            });
        });
    }
    function statText3() {
        var elem_nums = $(".provider-num");
        elem_nums.each(function (i, j) {
            $.ajax({
                type: "Post",
                url: "/getproviderCount",
                dataType: "json",
                success: function(data){
                    $("#rovider-num3").html(data)
                },
                error: function(err) {
                    alert(err);
                }
            });
        });
    }
    function statText4() {
        var elem_nums = $(".customar-num");
        elem_nums.each(function (i, j) {
            $.ajax({
                type: "Post",
                url: "/getcustomarnumCount",
                dataType: "json",
                success: function(data){
                    $("#customar-num4").html(data)
                },
                error: function(err) {
                    alert(err);
                }
            });
        });
    }

    /**
     * ????????????
     */
    var userSourceOption;
    var names = new Array();
    var nums = new Array();
    // var names=[];    //?????????????????????????????????X???????????????
    // var nums=[];    //?????????????????????????????????Y????????????
    $.ajax({
        type : "post",
        async : false,            //???????????????????????????????????????????????????????????????????????????????????????????????????????????????
        url : "/selectSalesName",    //???????????????TestServlet???
        dataType : "json",        //?????????????????????json
        success : function(result) {
            //???????????????????????????????????????result????????????????????????json??????
            if (result) {
                for (var i = 0; i < result.length; i++) {
                    names.push(result[i].goods.goodsName);    //???????????????????????????????????????
                }
                for (var i = 0; i < result.length; i++) {
                    nums.push(result[i].number);    //???????????????????????????????????????
                }
                userSourceOption = {
                    backgroundColor: "#38445E",
                    "grid": {
                        left: '12%',
                        top: '5%',
                        bottom: '12%',
                        right: '8%'
                    },
                    "xAxis": [{
                            data: names,
                            axisTick: {
                                show: false
                            },
                            axisLine: {
                                lineStyle: {
                                    color: 'rgba(255, 129, 109, 0.1)',
                                    width: 1 //????????????????????????????????????
                                }
                            },
                            axisLabel: {
                                textStyle: {
                                    color: '#999',
                                    fontSize: 12
                                }
                            }
                        }],
                    "yAxis": [{
                            splitNumber: 2,
                            axisTick: {
                                show: false
                            },
                            axisLine: {
                                lineStyle: {
                                    color: 'rgba(255, 129, 109, 0.1)',
                                    width: 1 //????????????????????????????????????
                                }
                            },
                            axisLabel: {
                                textStyle: {
                                    color: '#999'
                                }
                            },
                            splitArea: {
                                areaStyle: {
                                    color: 'rgba(255,255,255,.5)'
                                }
                            },
                            splitLine: {
                                show: true,
                                lineStyle: {
                                    color: 'rgba(255, 129, 109, 0.1)',
                                    width: 0.5,
                                    type: 'dashed'
                                }
                            }
                        }],
                    "series": [{
                            name: 'hill',
                            type: 'pictorialBar',
                            barCategoryGap: '0%',
                            symbol: 'path://M0,10 L10,10 C5.5,10 5.5,5 5,0 C4.5,5 4.5,10 0,10 z',
                            label: {
                                show: true,
                                position: 'top',
                                distance: 15,
                                color: '#DB5E6A',
                                fontWeight: 'bolder',
                                fontSize: 20,
                            },
                            itemStyle: {
                                normal: {
                                    color: {
                                        type: 'linear',
                                        x: 0,
                                        y: 0,
                                        x2: 0,
                                        y2: 1,
                                        colorStops: [{
                                            offset: 0,
                                            color: 'rgba(232, 94, 106, .8)' //  0%  ????????????
                                        },
                                            {
                                                offset: 1,
                                                color: 'rgba(232, 94, 106, .1)' //  100%  ????????????
                                            }
                                        ],
                                        global: false //  ?????????  false
                                    }
                                },
                                emphasis: {
                                    opacity: 1
                                }
                            },
                            data: nums,
                            z: 10}
                    ]
                }
            }
        },
        error : function(errorMsg) {
            //??????????????????????????????
            alert("????????????????????????!");
            myChart.hideLoading();
        },
    });

    /**
     *
     */
    function userSource() {
        var rankinglist = echarts.init($("#rankinglist")[0], "theme");
        rankinglist.setOption(userSourceOption);
        okUtils.echartsResize([rankinglist]);
    };

    /*
    *????????????
    * */
    layui.use('table', function() {
        var table = layui.table;
        table.render({
            method: "get",
            url: '/getgoodsNumber',
            elem: '#goodsNumber',
            height: 340,
            page: true,
            limit: 7,
            cols: [[
                {field:'id', title: 'ID', sort: true}
                ,{field:'goodsName', title: '????????????'} //width ???????????????????????????????????????????????????????????? minWidth ???????????????????????????????????????????????????layui 2.2.1 ??????
                ,{field:'number', title: '????????????', sort: true}
            ]]
        });
    });

    /*
    * ?????????????????????
    * */
    layui.use('table', function() {
        var table = layui.table;
        table.render({
            method: "get",
            url: '/getsalesNumber',
            elem: '#salesNumber',
            height: 380,
            page: true,
            limit: 10,
            cols: [[
                {field:'id', title: 'ID', sort: true}
                ,{field:'goodsName', title: '????????????',templet: "<div>{{d.goods.goodsName}}</div>"} //width ???????????????????????????????????????????????????????????? minWidth ???????????????????????????????????????????????????layui 2.2.1 ??????
                ,{field:'number', title: '????????????', sort: true}
            ]]
        });
    });

    /**
     * ????????????
     */
    layui.use('table', function() {
        var table = layui.table;
        table.render({
            method: "get",
            url: '/selectnoticelist',
            elem: '#userData',
            height: 380,
            page: true,
            limit: 10,
            cols: [[
                {field:'id', title: 'ID' }
                ,{field:'title', title: '??????'} //width ???????????????????????????????????????????????????????????? minWidth ???????????????????????????????????????????????????layui 2.2.1 ??????
                ,{field:'context', title: '??????'}
                ,{field:'time', title: '??????'}
                ,{field:'operateName', title: '?????????'}
            ]]
        });
    });


    /*
    * ????????????
    **/
    $.ajax({
        type: "Post",
        url: "/getnumber1",
        dataType: "json",
        success: function(data){
            $("#number11").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/getnumber2",
        dataType: "json",
        success: function(data){
            $("#number12").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/getnumber3",
        dataType: "json",
        success: function(data){
            $("#number13").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/getnumber4",
        dataType: "json",
        success: function(data){
            $("#number14").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/pages/inport/getnumber5",
        dataType: "json",
        success: function(data){
            $("#number15").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/pages/outport/getnumber6",
        dataType: "json",
        success: function(data){
            $("#number16").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/getnumber7",
        dataType: "json",
        success: function(data){
            $("#number17").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/getnumber8",
        dataType: "json",
        success: function(data){
            $("#number18").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });
    $.ajax({
        type: "Post",
        url: "/getnumber9",
        dataType: "json",
        success: function(data){
            $("#number19").html(data)
        },
        error: function(err) {
            alert(err);
        }
    });


    statText1();
    statText2();
    statText3();
    statText4();
    userSource();
});


