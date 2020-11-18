<!DOCTYPE html>
<html>

	<head>
		<meta charset="utf-8" />
		<title></title>
		<style>
			/*.tableTwo {
				margin-top: 40px;
				;
				border-spacing: 0px;
			}
			
			.line-limit-length {
				overflow: hidden;
				text-overflow: ellipsis;
				white-space: nowrap; //文本不换行，这样超出一行的部分被截取，显示...
			}
			*/

			
			.infoList {
				border-bottom: 1px solid #ccc;
			}
			
			li {
				list-style: none;
			}
			
			.container {
				width: 90%;
				margin: 0 auto;
			}
			
			.container>h4 {
				margin-bottom: 0px;
			}
			
			.infoTitle {
				padding-bottom: 10px;
				margin-top: 10px;
				margin-bottom: 0px;
			}
			
			.TitleInfo {
				padding-bottom: 10px;
				margin: 0px;
				border-bottom: 1px solid #ccc;
			}
			
			ul {
				padding-left: 0px;
			}
			
			.infoList {
				padding-bottom: 5px;
			}
			
			.infoList li h4 {
				margin: 0px
			}
			
			.infoList li p {
				margin: 8px 0px;
			}
			
			.infoList li {
				margin-bottom: 15px;
			}
			
			.warning {
				text-align: center;
			}
		</style>
	</head>

	<body>
		<div class="container">
			<h4>【预警标题】${title}</h4>
			<p class="infoTitle">
				<span>来自帐号：${userName}</span>
				<span style="margin-left:20px;display:inline-block">查看更多请登录<a href="${url}">${url}</a></span>
				<!--<span style="margin-left:20px;display:inline-block">查看更多请登录<a href="http://www.netinsight.com.cn/">http://www.netinsight.com.cn/</a></span>-->
			</p>
			<p class="TitleInfo">
				<span>此次预警共${size}条信息</span>
			</p>
			<ul class="infoList">
			<#list listMap as p>
			<li>
					<div class="leftContent">
						<h4><a href="${p.url!}">'${p.title!}'</a></h4>
						<!--content.replace(/\s|\xA0/g,'').length>200?content.slice(0,150)+ '...':content-->
						<p>'${p.content!}'</p>
					</div>
					<div class="rightContent">
						<span>${p.urlTime!}</span>
						<span style="margin-left:20px;display:inline-block">${p.source!}</span>
					</div>
				</li>
			</#list>
				
			</ul>
			<p class="warning">如您不想收到此邮件,请联系相关人员在站内设置取消发送。</p>
		</div>

		<!--<table>
			<tbody>
				<tr>
					<td style="position: absolute; left: 8px; right: 8px; top: 13px;padding-bottom: 15px; font-size: 15px;">
						[预警标题]${title}<br/> 此次预警共有${size}条信息，如果想查看更多详情,请登录
						<a>http://119.254.92.55:8084/</a>;<br/> 
					</td>
				</tr>
			</tbody>
			<table border="1" style="height: 47px;" class="tableTwo">
				<tr>
					<td align="center" class="line-limit-length">文章标题</td>
					<td align="center" style="width: 10px;">来源</td>
					<td align="center">发表时间 </td>
				</tr>
				<#list listMap as p>
					<tr>
						<td align="center">
							<a href="${p.url}" target="_blank" style="display:inline-block;width: 300px;" class="line-limit-length">${p.title}</a>
						</td>
						<td align="center" style="width: 408px">
							${p.source}
						</td>
						<td align="center">
							${p.urlTime}
						</td>
					</tr>
				</#list>
			</table>
			<tbody>
				<tr>
					<td></br>如您不想收到此邮件,请联系相关人员在站内设置取消发送。</td>
				</tr>
			</tbody>
		</table>-->
	</body>

</html>