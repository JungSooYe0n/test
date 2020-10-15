<!DOCTYPE html>
<html>
<meta name="viewport" content="width=device-width,height=device-height, user-scalable=no,initial-scale=1, minimum-scale=1, maximum-scale=1,target-densitydpi=device-dpi ">
<head>
    <style>
        table,table td,table th{border:1px solid #666;border-collapse:collapse;}
    </style>
</head>
<style type="text/css">
	.table-content{
		table-layout:fixed;
	}
	.table-content td{
		height: 50px;
		font-size: 1rem;
	}
	.text-right{
		text-align: right;
	}
	@media screen and (min-width: 760px) {
    .content {
        margin-left: 5%;
    }
}
</style>
<body>
	<span>
		您好：
	</span>
	<div class="content">
		<p>
			帐号到期提醒：帐号为<span>${username}(<i style="font-style: normal;font-weight: bold;">${organizationName}</i>)，服务到期时间为<span>${expireat}</span>,距离到期日剩余<strong style="color: rgb(246,0,0);">${n}</strong>天</span>
		</p>
		<table class="table-content">
        <tr>
            <td class="text-right" width="25%">登录帐号：</td>
            <td width="50%">${username}</td>
        </tr>
        <tr>
            <td class="text-right">用户昵称：</td>
            <td>${displayName}</td>
        </tr>
        <tr>
            <td class="text-right">机构名称：</td>
            <td>${organizationName}</td>
        </tr>
        <tr>
            <td class="text-right">联系方式：</td>
            <td>${phone}</td>
        </tr>
        <tr>
            <td class="text-right">邮箱地址：</td>
            <td>${email}</td>
        </tr>
        <tr>
            <td class="text-right">帐号到期日：</td>
            <td>${expireat}</td>
        </tr>
    	</table>
	</div>
    
</body>
</html>

