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
        <td style="position: absolute; left: 8%; right: 8%; height: 42%; top: 13%;padding-bottom: 15px; font-size: 15px;">
            亲爱的用户：
        </td>
        <td style="position: absolute; left: 10%; right: 10%; height: 42%; top: 20%;padding-bottom: 10px; font-size: 13px;">
            您好！您收到一封来自拓尔思的预警邮件
        </td>
		
			<td align="center" style="position: absolute; width: 302px; height: 52px; left: 0; right: 0; top: 32%; margin: auto;">       			
				<#list listMap as p>
					<a href="${p.url}"target="_blank">${p.title}</a> <br/>
				</#list>				
			</td>
		
    </tr>
    </tbody>
</table>
</body>
</html>
