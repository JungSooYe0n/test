$(function() {
  // 加载效果
  var ajaxbg = $("#background,#progressBar");
  $('.header').hide();
  $('.header1').hide();
  $('.header2').hide();
  // 定义一个groupName用来判断渲染哪种类型的详情
  var groupName = getUrlParam('groupName');
  // var BaseUrl = window.location.host;
  // 用来区别是否显示哪个头部 one表示新闻 two表示论坛
  var flag = '';
  // 微信图标是否显示
  var weixinShow = 'noweixin'
  // tf的时候header显示哪个图标
  var tfFlag = 'weibo';
  var data = {
    accessToken:'',
    sid:'',
    md5:'',
    trslk:''
  }
  data.accessToken = getUrlParam('accessToken');
  data.sid = getUrlParam('sid');
  data.md5 = getUrlParam('md5');
  data.trslk = getUrlParam('trslk');
  if(groupName!='weibo'&&groupName!='weixin'&&groupName!='twitter'&&groupName!='facebook') {
    data.nreserved1 = getUrlParam('nreserved1');
  }
  // rem设置
  function autoSize(width) {
    var width = width ? width: 720;
    var units = width / 100;
    var width = document.documentElement.clientWidth;
    width = width > 1080 ? 1080 : width;
    width = width <= 240 ? 240 : width;
    var calFontSize = width / units;
    document.documentElement.style.fontSize = calFontSize + "px"
  }
  autoSize();
  window.onresize = function() {
    autoSize()
  };
  ajaxFn();
  
  // url参数获取
  function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg);  //匹配目标参数
    if (r != null) return unescape(r[2]); return null; //返回参数值
  }
    function getUrl() {
        var Url = document.URL;
        var urlStr = Url.substring(0,Url.indexOf("/netInsight/"));
        return urlStr;
    }
  // 处理页面中的image标签
  function dealContent(str){
    // var aa = str.replace(/&lt;IMAGE&nbsp;SRC=&quot;/g,'<img src="').replace(/&quot;&nbsp;&gt;/g,'" style="max-width: 100%;">').replace(/↵/g, "");
    // var aa = str.replace(/↵/g, "").replace(/&nbsp;/g, " ").replace(/&lt;IMAGE SRC=&quot;/g,'<img src="').replace(/&lt;IMAGESRC=&quot;/g,'<img src="').replace(/&quot;&gt;/g,'" style="max-width: 100%;">');
    var aa = str.replace(/&lt;/g, "<").replace(/&nbsp;/g, " ").replace(/&quot;/g, "'").replace(/&gt;/g, " style='max-width: 100%;' >").replace(/[\n]/g, "<br/>")
    return aa;
  }
  // 判断哪一个头部显示
  function showHead() {
    if(flag == 'two') {
      // 论坛
      $('.header').hide();
      $('.header1').hide();
      $('.header2').show();
    } else if(flag=='one'){
      $('.header').hide();
      $('.header1').show();
      $('.header2').hide();
      
    } else if(groupName=='weibo'||groupName=='twitter'||groupName=='facebook') {
      $('.header').show();
      $('.header1').hide();
      $('.header2').hide();
      // 用来显示哪种logo
      if(tfFlag == 'weibo') {
        $('#logo').show();
        $('#logo1').hide();
        $('#logo2').hide();
        $('.icon3').hide();
      } else if(tfFlag == 'twitter') {
        $('#logo').hide();
        $('#logo1').hide();
        $('#logo2').show();
        $('.icon3').show();
      } else {
        $('#logo').hide();
        $('#logo1').show();
        $('#logo2').hide();
        $('.icon3').show();
      }
    } else {
      $('.header').hide();
      $('.header1').show();
      $('.header2').hide();
    }
    // 微信的标志是否显示
    if(weixinShow == 'weixin') {
      $('.header1 .icon').show();
    } else {
      $('.header1 .icon').hide();
    }
  }
  // ajax渲染页面
  function ajaxFn(){
    var url = '';
    if(groupName=='weibo') {
      url = getUrl()+'/netInsight/app/api/oneInfoStatus';
      tfFlag = 'weibo'
    } else if(groupName=='twitter'||groupName=='facebook') {
      url = getUrl()+'/netInsight/app/api/oneInfoTF';
      tfFlag = groupName
    } else if(groupName=='weixin') {
      url = getUrl()+'/netInsight/app/api/oneInfoWeChat';
      weixinShow = 'weixin'
    } else {
      // 论坛 新闻等
      url = getUrl()+'/netInsight/app/api/oneInfo';
      weixinShow = 'noweixin'
    }
    $.ajax({
      url:url,
      type:'post',
      data:data,
      dataTypt:'json',
      beforeSend:function()
      {  
          ajaxbg.show(); 
      },
      error: erryFunction,
      success: succFunction
    })
  }
  function erryFunction(err) {
    ajaxbg.hide();
    console.log(err)
  }
  function succFunction(tt) {
      if (tt.code == 200){
        ajaxbg.hide();
        if(groupName=='weibo'||groupName=='twitter'||groupName=='facebook') {
          var jsData = tt.data[0][0];
          var time = jsData.urlTime?jsData.urlTime:jsData.createdAt
          $('#time').html(formatDateTime(time))
          $('#content').html(dealContent(jsData.statusContent))
          $('#name').html(jsData.screenName)
          $('#reply').html(jsData.rttCount)
          $('#commit').html(jsData.commtCount)
          // 点赞数
          $('#dianzan').html(jsData.approveCount)
        } else if(groupName=='weixin'){
          var jsData = tt.data[0][0];
          $('.authors').html(jsData.authors)
          $('#urlTitle').html(jsData.urlTitle)
          console.log(dealContent(jsData.content))
          $('#content').html(dealContent(jsData.content))
          $('.time').html(formatDateTime(jsData.urlTime))
        } else {
          // 论坛展示 或者新闻
          if(data.nreserved1=='0') {
            var jsData = tt.data.mainCard;
          } else if(data.nreserved1=='1') {
            var jsData = tt.data.replyCard[0];
          } else {
            var jsData = tt.data[0][0];
          }
          console.log(jsData)
          if(jsData.groupName=='国内论坛') {
            flag = 'two';
            $('#title').html(jsData.title);
            $('.urlTime').html(formatDateTime(jsData.urlTime));
            $('#siteName').html(jsData.siteName);
            $('#channel').html(jsData.channel)
            $('#authors').html(jsData.authors)
            $('#content').html(dealContent(jsData.content))
          } else {
            flag = 'one';
            var authors = jsData.authors?jsData.authors:jsData.siteName
            $('.authors').html(authors)
            $('#urlTitle').html(jsData.urlTitle)
            $('#content').html(dealContent(jsData.content))
            $('.time').html(formatDateTime(jsData.urlTime))
          }
        }
        showHead();
      }else {
        ajaxbg.hide();
        console.log(tt)
      }
  }
  // 时间戳转换
  function formatDateTime (inputTime) {
    var date = new Date(inputTime);
    var y = date.getFullYear();
    var m = date.getMonth() + 1;
    m = m < 10 ? ('0' + m) : m;
    var d = date.getDate();
    d = d < 10 ? ('0' + d) : d;
    var h = date.getHours();
    h = h < 10 ? ('0' + h) : h;
    var minute = date.getMinutes();
    var second = date.getSeconds();
    minute = minute < 10 ? ('0' + minute) : minute;
    second = second < 10 ? ('0' + second) : second;
    return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
  }
  // tab栏切换
  $('#tit>span').click(function() {
    var i = $(this).index();//下标第一种写法
    //var i = $('tit').index(this);//下标第二种写法
    $(this).addClass('select').siblings().removeClass('select');
    $('#con>li').eq(i).show().siblings().hide();
  });
});

