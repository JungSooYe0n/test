<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8"/>
    <title></title>
</head>
<body>
<table style="width: 508px; height: 478px;">
    <tbody style="width: 508px; height: 478px;">
    <tr>
        <td style="position: absolute; left: 8px; right: 8px; height: 45px; top: 13px;padding-bottom: 15px; font-size: 15px;">
            亲：
        </td>
	</tr>
	<tr>
        <td style="position: absolute; left: 90px; right: 10px; height: 45px; top: 13px;padding-bottom: 10px; font-size: 13px;">
            有人创建了一个专项 OR 预警轮播需要找图片
        </td>
	</tr>
	<tr>
		<td align="center" style="position: absolute; width: 602px; height: 52px; left: 0; right: 0; top: 32px; margin: auto;">       			
			<#list listMap as p>
			名是：${p.title}<br/>
			表达式：${p.url} <br/>								
			</#list>				
		</td>		
    </tr>
    </tbody>
</table>
</body>
</html>
