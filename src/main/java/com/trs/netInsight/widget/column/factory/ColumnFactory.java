package com.trs.netInsight.widget.column.factory;

import com.trs.netInsight.handler.result.CommonResult;
import org.apache.commons.lang3.StringUtils;

import com.trs.netInsight.config.constant.ColumnConst;
import com.trs.netInsight.util.ObjectUtil;
import com.trs.netInsight.widget.column.entity.IndexTabType;

import lombok.extern.slf4j.Slf4j;

/**
 * 栏目构造工厂类
 * 
 * @author 北京拓尔思信息技术股份有限公司
 * @since changjiang @ 2018年4月4日
 *
 */
@Slf4j
public class ColumnFactory {

	/**
	 * @Desc : 根据栏目类型构造对应栏目
	 * @since changjiang @ 2018年4月8日
	 * @param typeCodeArray
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @Return : AbstractColumn
	 */
	@SuppressWarnings("unchecked")
	public static AbstractColumn createColumn(String[] typeCodeArray) {
		try {
			if (ObjectUtil.isNotEmpty(typeCodeArray)) {
				String typeCode = "";
				if (typeCodeArray.length > 1 || typeCodeArray[0].equals(ColumnConst.LIST_STATUS_COMMON) || typeCodeArray[0].equals(ColumnConst.LIST_WECHAT_COMMON)
						|| typeCodeArray[0].equals(ColumnConst.LIST_TWITTER) || typeCodeArray[0].equals(ColumnConst.LIST_FaceBook)) {
					typeCode = ColumnConst.LIST_NO_SIM;
				}else {
					typeCode = typeCodeArray[0];
				}
				IndexTabType type = chooseType(typeCode);
				Class<AbstractColumn> forName;
				forName = (Class<AbstractColumn>) Class.forName(type.getResource());
				AbstractColumn newInstance = forName.newInstance();
//				ColumnConst.LIST_STATUS_COMMON.equals(StringUtils.join(typeCodeArray, ";"));
				if(ColumnConst.LIST_STATUS_COMMON.equals(StringUtils.join(typeCodeArray, ";"))){
					newInstance.setListOnlyStatus(true);
				}
				return newInstance;
			}
		} catch (Exception e) {
			log.error("created column error", e);
		}
		return null;
	}

	/**
	 * @Desc : 根据栏目类型选择对应的枚举类
	 * @since changjiang @ 2018年4月8日
	 * @param typeName
	 * @return
	 * @Return : IndexTabType
	 */
	private static IndexTabType chooseType(String typeCode) {
		for (IndexTabType indexTybe : IndexTabType.values()) {
			if (indexTybe.getTypeCode().equals(typeCode)) {
				return indexTybe;
			}
		}
		return null;
	}

	/**
	 * 根据栏目类型代码直接返回示例结果 集
	 * @param typeCodeArray
	 * @return
	 */
	public static Object directByType(String[] typeCodeArray){

		String typeCode = ColumnConst.LIST_NO_SIM;
		CommonResult result = new CommonResult();


		if (typeCodeArray.length > 1 || typeCodeArray[0].equals(ColumnConst.LIST_STATUS_COMMON) || typeCodeArray[0].equals(ColumnConst.LIST_WECHAT_COMMON)
				|| typeCodeArray[0].equals(ColumnConst.LIST_TWITTER) || typeCodeArray[0].equals(ColumnConst.LIST_FaceBook)) {
			typeCode = ColumnConst.LIST_NO_SIM;
		}else {
			typeCode = typeCodeArray[0];
		}

		if (typeCode.equals(ColumnConst.LIST_NO_SIM)){//列表
			result.setData("[{simCount:163163,groupName:微博,timeAgo:2018.07.16 17:23:16,md5Tag:0e39cb3ca47ac7ea,hkey:13402574340160682874,siteName:姐姐的小熊仔,nreserved1:null,isNew:false,title://@孟美岐爆肝数据站:#孟美岐[超话]##和孟美岐一起听首歌#&nbsp;&nbsp;新的打榜活动，快来为美岐赢得应援金福利吧[兔子]@火箭少女101_孟美岐//@唯饭娱乐:#唯饭娱乐粉丝团#&nbsp;唯饭娱乐<font color=red>APP</font>七月粉丝团活动来啦，依旧是五万应援现金和<font color=red>APP</font>开屏曝光等奖励，这次还设置了奖励奖和参与奖，快艾特自家爱豆的粉丝团来参与吧~【活动报名】&nbsp;参与活动的粉丝团转发该条微博，再私信唯饭娱乐官微要入驻的明星圈子名称即可申请入驻（若<font color=red>APP</font>内搜不到，请私信联系开通圈子）粉丝团转发该条微博，转发一万可<font color=red>得到</font>一万朵鲜花（<font color=red>APP</font>内），单个粉丝团上限一万*同一明星多个粉丝团，则率先转发达到1万的前三个粉丝团可获得一万鲜花，转满一万后截图私信，以私信的先后时间为准,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4262459487754643},{simCount:80695,groupName:微博,timeAgo:2分钟前,md5Tag:679c707b0d378d18,hkey:9546781363309029291,siteName:羽毛儿啵啵吖,nreserved1:null,isNew:true,title://@羽毛儿啵啵吖:周彦辰冲呀//@周漂亮的小v脸:安排！//@PATRON_周彦辰的护花大队:#周彦辰[超话]#&nbsp;#全能偶像周彦辰#&nbsp;&nbsp;&nbsp;一万[拳头]@周彦辰//@唯饭娱乐:#唯饭娱乐粉丝团#&nbsp;唯饭娱乐<font color=red>APP</font>七月粉丝团活动来啦，依旧是五万应援现金和<font color=red>APP</font>开屏曝光等奖励，这次还设置了奖励奖和参与奖，快艾特自家爱豆的粉丝团来参与吧~【活动报名】&nbsp;参与活动的粉丝团转发该条微博，再私信唯饭娱乐官微要入驻的明星圈子名称即可申请入驻（若<font color=red>APP</font>内搜不到，请私信联系开通圈子）粉丝团转发该条微博，转发一万可<font color=red>得到</font>一万朵鲜花（<font color=red>APP</font>内），单个粉丝团上限一万，截止至7月19日11:00*同一明星多个粉丝团，则率先转发达到1万的前三个粉丝团可获得一万鲜花，转满一万后截图私信，以私信的先后时间为准活动报名截止至8月6日上午10:00,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264911095267535},{simCount:37001,groupName:微博,timeAgo:2小时前,md5Tag:334ffb174ce0d0d2,hkey:7878156645458967442,siteName:尘埃_MARCHT,nreserved1:null,isNew:true,title://@riiverr:&nbsp;//@R三十://@漆雕凌:希望他长大后不要忘了//@仙宫一枝花裸基小公主://@阿腐要现充://@_桃内纯子_:最后一条//@芥菜是一棵菜:深邃的思想不一定需要成熟的肉体。//@jillhear:儿子的谈话，一年来的记录。赚钱“你觉得将来什么最赚钱？”儿子说：“思想最赚钱。”“你认为你选择什么专业既能赚钱、又能满足你对成功的向往？”儿子说：“所有都可以。成功靠得是放肆，而不是规矩。当你按照现有规律去思考问题时，你可能会错过更多。”人类的恐惧儿子说：“人类是低等的生物。因为人类总是活在恐惧之中，是恐惧割裂了人与自然的联系。而动物总是顺其自然。”“割裂了与自然的联系就是低等生物吗？”“只有自然才能给我们智慧，人们自以为是的聪明阻碍了自己。人因为恐惧发明了鬼与神，当人们受到挫折时，人们会把责任推给鬼；当人们获得成功时，会认为是神在保佑他。所以人不能清楚地认识自己的能力。”焦虑我问：“怎样才能消除焦虑呢？我看你玩了一天不写作业……”儿子说：“只要你真正理解一个事物的发展规律，任何事物都可以，你只要理解其中一件，你就会明白。”课本饭后儿子为我读了多篇语文课文，我问他对许多语文课文包含德育目的有什么看法，他说：“我注意到我们的书虽然是2018年印刷的，但它的版次是2004年，那时我还没有出生。也许那时推广德育的途径不如现在这么多样，但我们现在已经能从广告、电影、网络等各种形式中接收到，现在的孩子接受信息的渠道太广泛了，课本不用承担太多的责任。”梵高儿子看了四年级美术课本上关于梵高生平和作品的介绍，我们讨论“梵高生前为什么没有<font color=red>得到</font>认可”，儿子分析：“虽然当时的人们和现在的人们一样能够接收到梵高作品里的奔放、粗鲁、同情、生机，但他们对自己的审美失去了信心，他们的不自信让他们的审美滞后了。”自卑“自卑是怎么产生的呢？”儿子说：“当你想做什么事时，你周围的人预言了你的失败。尤其是你的亲人给你的预言对你影响更大。”记忆“用大脑记住的<font color=red>知识</font>容易丢失，只有和心灵链接的<font color=red>知识</font>，才能成为永恒的记忆。”写着数学作业的儿子说。经验与艺术家周末，儿子说：“人们需要经验的指导，就像大雪天在前人的脚印上行走。”“艺术家要舍得摧毁自己的作品。只有摧毁才能为重建提供空间。”自闭儿子说：“自闭症的孩子比普通的孩子聪明一倍。因为他们与自然的链接并没有被切断，也不会被众多信息所误导和污染。”挖掘儿子说：“如果你在一个深坑里挖到一个宝贝，你是把它扔了，还是据为己有？”我说：“当然揣兜里了。”儿子说：“我会把它扔了，继续挖掘。这样才不会被它控制，因为我的目的是挖掘。”控制权儿子说：“当一个新思想和你的思想碰撞，你可能会被新思想控制，你轻易就把控制权交给了别人，这也是国家和一些领头人存在的原因。但控制你的应该是你的身体。”本质昨天听完儿子的演说，我跟他说：“你的认知水平已经超过了妈妈，因为你能看到事物的本质。”“本质，本质，你们总是说“本质”这个词，好像你们懂得本质似的，可是你们知道什么是本质吗？”我感觉有点懵。我没有思考过。他接着说：“本质，是人和事物之间的一种契约。人只能使用它们，但是带不走。为什么我们要读小学、中学、大学、硕士、博士，甚至一辈子？是因为我们和<font color=red>知识</font>的契约，需要我们拿这么长的时间来交换。我就算再聪明，也挣脱不了这样的命运，这是人类共同的命运。”教与育儿子举例谈“教”和“育”的区别：“老师教你画蝴蝶，你画出了蝴蝶，这是教。老师教你画蝴蝶，但你通过联想，最终画了一张网，这是育。”教师儿子谈教育和教师的任务：“教育必须将“教”和“育”统一。教，是让学生和<font color=red>知识</font>发生联系。而育，必须像春风一样，保护学生的求知欲和好奇心。”“老师的义务有三个层次。传授<font color=red>知识</font>只是最基本的义务。第二个层次，老师要成为一把锁，锁住学生和<font color=red>知识</font>之间的链条。但锁总是会生锈的，这就需要老师完成第三个任务，就是把打开这把锁的钥匙，交给学生。而学生从此再也不需要老师。”“幼儿园的义务是生长、活动和保护。孩子们的心只有同龄人能够感受。老师要更多地做到“育”，而不是“教”。”“小学的义务，是建立学生与<font color=red>知识</font>的契约，需要教和育相互结合。没有“育”，只有“教”的教育，那和“说”没有区别。”问题儿子：“世上本来没有美丑之分，美丑都是人定的。”“世上本来也没有问题，有了人之后就有了问题，人们才有了事做。”自制力儿子说：“只有愿望不强烈或者感觉自己做不到的人才需要自制力来约束自己。真正的强者根本不需要自制力，他们想到什么就去做了。”,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264881244940639},{simCount:34869,groupName:微博,timeAgo:2分钟前,md5Tag:73e36476a61b8f38,hkey:13036454942681334800,siteName:6628左右捎,nreserved1:null,isNew:true,title:[吃瓜]//@欧阳文锋0662:看剧别忘听歌#爱的数学公式[音乐]#&nbsp;#亚洲新歌榜#//@爱奇艺文学:【限时抢！熊梓淇&nbsp;&nbsp;签名照】#熊梓淇我和两个他#&nbsp;今晚20:00就在爱奇艺播出啦！让我们和天才少年肖恩，一起攻略爱情这道题关注爱奇艺文学&nbsp;转发这条微博说出你爱他的理由~热转（转发量）前三、前三、前三的小伙伴就能<font color=red>得到</font>签名照一张啦！截止时间：7月27日&nbsp;15:00爱奇艺阅读<font color=red>APP</font>同名小说免费读，阅读链接送给你http://t.cn/Rn0zkV8[心]@熊梓淇&nbsp;@我和两个他&nbsp;@熊梓淇内地后援会&nbsp;http://t.cn/Rgobc5C,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264911187585990},{simCount:34771,groupName:微博,timeAgo:1小时前,md5Tag:6038e321932b5568,hkey:3538128653645483456,siteName:用户6591688267,nreserved1:null,isNew:true,title:[好喜欢]//@撩熊2:#亚洲新歌榜#&nbsp;@熊梓淇&nbsp;没有理由#爱的数学公式[音乐]#//@爱奇艺文学:【限时抢！熊梓淇&nbsp;&nbsp;签名照】#熊梓淇我和两个他#&nbsp;今晚20:00就在爱奇艺播出啦！让我们和天才少年肖恩，一起攻略爱情这道题关注爱奇艺文学&nbsp;转发这条微博说出你爱他的理由~热转（转发量）前三、前三、前三的小伙伴就能<font color=red>得到</font>签名照一张啦！截止时间：7月27日&nbsp;15:00爱奇艺阅读<font color=red>APP</font>同名小说免费读，阅读链接送给你http://t.cn/Rn0zkV8[心]@熊梓淇&nbsp;@我和两个他&nbsp;@熊梓淇内地后援会&nbsp;http://t.cn/Rgobc5C,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264892955374860},{simCount:28123,groupName:微博,timeAgo:5分钟前,md5Tag:77fff44ef76a2968,hkey:3704373716517385505,siteName:用户6606089841,nreserved1:null,isNew:true,title:[鲜花]//@唯饭娱乐:#唯饭娱乐粉丝团#&nbsp;唯饭娱乐<font color=red>APP</font>七月粉丝团活动来啦，依旧是五万应援现金和<font color=red>APP</font>开屏曝光等奖励，这次还设置了奖励奖和参与奖，快艾特自家爱豆的粉丝团来参与吧~【活动报名】&nbsp;参与活动的粉丝团转发该条微博，再私信唯饭娱乐官微要入驻的明星圈子名称即可申请入驻（若<font color=red>APP</font>内搜不到，请私信联系开通圈子）粉丝团转发该条微博，转发一万可<font color=red>得到</font>一万朵鲜花（<font color=red>APP</font>内），单个粉丝团上限一万，截止至7月19日11:00*同一明星多个粉丝团，则率先转发达到1万的前三个粉丝团可获得一万鲜花，转满一万后截图私信，以私信的先后时间为准活动报名截止至8月6日上午10:00,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264910529626812},{simCount:23178,groupName:微博,timeAgo:7分钟前,md5Tag:d8c59d3aa5ad39c3,hkey:6571426434351125718,siteName:zou520184,nreserved1:null,isNew:true,title:转发微博//@最佳蹲坑读物:《断舍离&nbsp;你需要扔掉的100样东西》想断舍离，先从扔掉收纳工具开始。第一步：首先，让我们从衣柜开始1、只剩一只的袜子、有洞的和没松紧的袜子。2、干洗店和买衣服时送的廉价衣架。3、不戴了的眼镜和太阳镜。不戴了的廉价首饰。4、不戴的围巾、和帽子。5、松松垮垮的旧内裤和BRA。6、发黄的t恤衫。7、那件因为起球就没穿几次的毛衣。8、便宜且过时的大衣。9、学生时代的短裙。穿起来很丑的衣服。10、别人送的不合身形的连衣裙。11、不再舒适的家居服和睡衣。孩子小时候的衣服留上几件做纪念就好。旧钱包。坏掉的表。衣柜中，尽量保证20件衣物以下，扔一件再买一件的原则。第二步：整理完衣柜，是不是很有成就感？接下来，就是鞋柜了1、3年没有穿过的运动鞋2、磨脚且不舒服的漂亮高跟鞋，可以送给适合的人。3、后跟磨掉不再舒服的鞋子。4、坏掉的拖鞋。5、多余的鞋盒。6、不用的鞋垫，增高垫。用不到的鞋带。第三步：床上用品篇1、旧的枕巾。2、不再舒服的枕头。枕头最好6个月更新一次。3、不愿再使用的床单。4、拉锁坏掉的被罩。5、不再暖和的毛毯。第四步：化妆品，每个女人每日必用品：1、不再舒服的毛巾。2、旧的不想再用的化妆品、过期的化妆品。3、干掉的指甲油。4、没挤干净的润肤露和牙膏。5、从宾馆拿回来的洗漱套装。6、各类没用完，落上灰的护肤试用小样。7、去年的防晒霜。8、没有效用不想再用的面膜。9、好几年也没想起来用的去角质产品。10、没用过几次的几年前的口红。那个“总有一天你会用到”的蓝色眼影。11、剩下瓶底且并不再用的乳液。12、空了的化妆品包装和瓶子。13、你根本不喜欢的旧香水。如果味道你还能接受，可以用来喷香卫生间。14、没弹性的头绳和不再佩戴的饰品。不用的卷发棒直发器。其实直接去理发店很方便。损坏一角不再使用的化妆镜。15、用过几个月有渍迹的化妆棉。第五步：一个家庭，厨房更是我们经常使用的地方1、冰箱里的过期食物。2、过期的优惠券。3、不用的扫帚和拖把。4、过多囤积的塑料袋。5、放了很久的吸管和一次性餐具。6、刷不干净的有裂纹的碟子和碗。7、放了很久都不想吃的零食。8、旧厨具比如有点不灵的高压锅。9、脏兮兮的抹布和洗碗巾。抹布一般是3个月更新一次，洗碗布1个月更新一次。10、不看的菜谱书。11、根本不用的玻璃杯。不再使用的饮水杯子。12、落灰的外卖单。以后这种东西不要拿。13、三年都没用完的调味料14、你都不知道放多久了的糖。第六步：书房篇，整洁的工作环境有助于专注力的提升：1、从来不看的旧课本。（我知道你怀念青春，但该放手了。）2、过去工作时候的东西。（留着证书就行，别的丢掉吧。）3、不听的CD不看了的DVD。4、去年的日历。5、不再需要的旧数据线。6、已经不用的老电脑、手机和各种旧家电。7、旧杂志、买回来从来没看过的书。8、你的旧日程本和根本没有在用的。9、在抽屉里等着扎你的大头针。10、一大堆没用的书签。11、旧电池。12、已经涂完的《秘密花园》。13、拼图。14、旅行宣传册，留下的电影票、景区门票、交通车票。。15、失去粘性的便利贴。16、各种不再需要的会员卡。17、废旧的稿纸。18、不出水的水笔。19、折旧的装饰品。20、储存的各类包装纸。21、旧纸箱和各种包装，有保修卡就行了。22、生锈的扳手和螺丝刀。&nbsp;23、干了的502。&nbsp;24、退订所有广告电子邮件。&nbsp;25、已经不再需要的各种小票。&nbsp;&nbsp;第七步：玩具篇：&nbsp;&nbsp;1、你家喵星人汪星人不喜欢的玩具。&nbsp;2、变样子的玩偶。&nbsp;3、家里小孩已经不用的各种玩意。&nbsp;4、坏掉的和洗不出来的玩具。&nbsp;小孩子的东西一般都用时不长，如果可以放到二手市场卖掉，更好。&nbsp;&nbsp;第八步：手机设备篇&nbsp;&nbsp;1、下载后没再打开过，或使用频率很少的<font color=red>APP</font>。&nbsp;2、朋友圈里刷屏的微商。&nbsp;3、社交软件里不认识的陌生人。&nbsp;4、微信里不感兴趣的账号。&nbsp;5、手机内存的不再看的视频。&nbsp;3、坏掉的充电宝，不用的旧手机。&nbsp;4、旧的手机壳。&nbsp;&nbsp;第九步：什物篇&nbsp;&nbsp;1、过期药！已经黏糊糊的止咳药水。&nbsp;2、抽屉里的不再使用眼镜布。&nbsp;3、不再使用的钥匙。&nbsp;4、零钱。（集了一罐就去银行换出来吧~）&nbsp;5、已经很久没用过的老地毯。&nbsp;6、装饰用蜡烛，你现在不用一辈子都不会用了。&nbsp;7、你总以为能用<font color=red>得到</font>的打包带和泡泡纸。要用就赶紧用，不用直接扔掉。&nbsp;8、几乎坏掉的行李箱。&nbsp;9、你都不知道有什么用的旧钥匙和材质很差的钥匙扣。&nbsp;10、空花盆。（要么种上花，要么扔了它。）&nbsp;11、不种花的话浇水壶也扔掉。&nbsp;12、已经枯萎或者快要死掉的绿植。&nbsp;&nbsp;内心篇&nbsp;&nbsp;1、以往的难过的经历：受过的坎坷，委屈，冷遇。&nbsp;2、伤害你的人、不适合的人。&nbsp;3、一个你并不爱的人。一个不爱你的人。&nbsp;4、前段恋情的遗物。&nbsp;&nbsp;算下来可能不止100件，需要扔的东西越多，说明你需要整理的个人空间越多。,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264910051453824},{simCount:22999,groupName:微博,timeAgo:2018.07.16 17:18:11,md5Tag:30d4106298e65d9e,hkey:8134776313004783967,siteName:喵了个咪_liurongl,nreserved1:null,isNew:false,title:可爱性感孟美岐[鲜花]//@唯饭娱乐:#唯饭娱乐粉丝团#&nbsp;唯饭娱乐<font color=red>APP</font>七月粉丝团活动来啦，依旧是五万应援现金和<font color=red>APP</font>开屏曝光等奖励，这次还设置了奖励奖和参与奖，快艾特自家爱豆的粉丝团来参与吧~【活动报名】&nbsp;参与活动的粉丝团转发该条微博，再私信唯饭娱乐官微要入驻的明星圈子名称即可申请入驻（若<font color=red>APP</font>内搜不到，请私信联系开通圈子）粉丝团转发该条微博，转发一万可<font color=red>得到</font>一万朵鲜花（<font color=red>APP</font>内），单个粉丝团上限一万*同一明星多个粉丝团，则率先转发达到1万的前三个粉丝团可获得一万鲜花，转满一万后截图私信，以私信的先后时间为准,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4262458204080171},{simCount:22623,groupName:微博,timeAgo:9小时前,md5Tag:c2dd02362811b231,hkey:9160058541297187330,siteName:用户6607898394,nreserved1:null,isNew:true,title:pick熊梓淇！pick熊梓淇！pick熊梓淇！//@歹人_Zhou:看剧的时候听听歌#爱的数学公式[音乐]#淇淇新歌好好听[米奇比心][米奇比心]#亚洲新歌榜#//@爱奇艺文学:【限时抢！熊梓淇&nbsp;&nbsp;签名照】#熊梓淇我和两个他#&nbsp;今晚20:00就在爱奇艺播出啦！让我们和天才少年肖恩，一起攻略爱情这道题关注爱奇艺文学&nbsp;转发这条微博说出你爱他的理由~热转（转发量）前三、前三、前三的小伙伴就能<font color=red>得到</font>签名照一张啦！截止时间：7月27日&nbsp;15:00爱奇艺阅读<font color=red>APP</font>同名小说免费读，阅读链接送给你http://t.cn/Rn0zkV8[心]@熊梓淇&nbsp;@我和两个他&nbsp;@熊梓淇内地后援会&nbsp;http://t.cn/Rgobc5C,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264768502051390},{simCount:18694,groupName:微博,timeAgo:2018.07.21 16:01:32,md5Tag:8f5c7a54e6d476f5,hkey:1951456105307643141,siteName:用户6461181211,nreserved1:null,isNew:false,title://@Dear迪丽热巴反黑站:#迪丽热巴[超话]#&nbsp;#迪丽热巴一千零一夜#&nbsp;新活动get[馋嘴]快来转发为小热巴领取奖励吧[米奇比心]@Dear-迪丽热巴//@唯饭娱乐:#唯饭娱乐粉丝团#&nbsp;唯饭娱乐<font color=red>APP</font>七月粉丝团活动来啦，依旧是五万应援现金和<font color=red>APP</font>开屏曝光等奖励，这次还设置了奖励奖和参与奖，快艾特自家爱豆的粉丝团来参与吧~【活动报名】&nbsp;参与活动的粉丝团转发该条微博，再私信唯饭娱乐官微要入驻的明星圈子名称即可申请入驻（若<font color=red>APP</font>内搜不到，请私信联系开通圈子）粉丝团转发该条微博，转发一万可<font color=red>得到</font>一万朵鲜花（<font color=red>APP</font>内），单个粉丝团上限一万，截止至7月19日11:00*同一明星多个粉丝团，则率先转发达到1万的前三个粉丝团可获得一万鲜花，转满一万后截图私信，以私信的先后时间为准活动报名截止至8月6日上午10:00,trslk:077eeb35-ed58-4118-a15a-19eb6f538625,sid:4264250853114792}]");
		}else if (typeCode.equals(ColumnConst.CHART_LINE)){//走势图
			result.setData("[{groupName:百度贴吧,data:[{fieldValue:2018/07/17,count:1148},{fieldValue:2018/07/18,count:1590},{fieldValue:2018/07/19,count:1295},{fieldValue:2018/07/20,count:1031},{fieldValue:2018/07/21,count:744},{fieldValue:2018/07/22,count:567},{fieldValue:2018/07/23,count:100}]},{groupName:东方头条网,data:[{fieldValue:2018/07/17,count:355},{fieldValue:2018/07/18,count:321},{fieldValue:2018/07/19,count:204},{fieldValue:2018/07/20,count:167},{fieldValue:2018/07/21,count:114},{fieldValue:2018/07/22,count:99},{fieldValue:2018/07/23,count:38}]},{groupName:新浪新闻客户端,data:[{fieldValue:2018/07/17,count:138},{fieldValue:2018/07/18,count:126},{fieldValue:2018/07/19,count:63},{fieldValue:2018/07/20,count:83},{fieldValue:2018/07/21,count:97},{fieldValue:2018/07/22,count:48},{fieldValue:2018/07/23,count:5}]},{groupName:搜狐新闻客户端,data:[{fieldValue:2018/07/17,count:53},{fieldValue:2018/07/18,count:102},{fieldValue:2018/07/19,count:81},{fieldValue:2018/07/20,count:39},{fieldValue:2018/07/21,count:7},{fieldValue:2018/07/22,count:36},{fieldValue:2018/07/23,count:3}]},{groupName:百度新闻客户端,data:[{fieldValue:2018/07/17,count:39},{fieldValue:2018/07/18,count:58},{fieldValue:2018/07/19,count:33},{fieldValue:2018/07/20,count:31},{fieldValue:2018/07/21,count:24},{fieldValue:2018/07/22,count:19},{fieldValue:2018/07/23,count:15}]},{groupName:洱海新闻网,data:[{fieldValue:2018/07/17,count:53},{fieldValue:2018/07/18,count:39},{fieldValue:2018/07/19,count:23},{fieldValue:2018/07/20,count:30},{fieldValue:2018/07/21,count:25},{fieldValue:2018/07/22,count:0},{fieldValue:2018/07/23,count:0}]},{groupName:新浪网,data:[{fieldValue:2018/07/17,count:47},{fieldValue:2018/07/18,count:41},{fieldValue:2018/07/19,count:30},{fieldValue:2018/07/20,count:24},{fieldValue:2018/07/21,count:8},{fieldValue:2018/07/22,count:9},{fieldValue:2018/07/23,count:8}]},{groupName:惠爱你,data:[{fieldValue:2018/07/17,count:0},{fieldValue:2018/07/18,count:71},{fieldValue:2018/07/19,count:54},{fieldValue:2018/07/20,count:4},{fieldValue:2018/07/21,count:0},{fieldValue:2018/07/22,count:6},{fieldValue:2018/07/23,count:5}]");
		}else if (typeCode.equals(ColumnConst.CHART_BAR_BY_META)){ //柱状图
			result.setData("[{groupName:国内新闻,num:5923,group:国内新闻},{groupName:微博,num:3365404,group:微博},{groupName:微信,num:46325,group:微信},{groupName:国内新闻_手机客户端,num:5734,group:国内新闻_手机客户端},{groupName:国内论坛,num:6527,group:国内论坛},{groupName:国内博客,num:152,group:国内博客},{groupName:国内新闻_电子报,num:193,group:国内新闻_电子报},{groupName:境外媒体,num:214,group:境外媒体},{groupName:Twitter,num:0,group:Twitter},{groupName:FaceBook,num:0,group:FaceBook}]");
		}else if (typeCode.equals(ColumnConst.CHART_PIE_BY_META)){ //饼状图
			result.setData("[{groupName:国内新闻,num:5923,group:国内新闻},{groupName:微博,num:3365404,group:微博},{groupName:微信,num:46325,group:微信},{groupName:国内新闻_手机客户端,num:5734,group:国内新闻_手机客户端},{groupName:国内论坛,num:6527,group:国内论坛},{groupName:国内博客,num:152,group:国内博客},{groupName:国内新闻_电子报,num:193,group:国内新闻_电子报},{groupName:境外媒体,num:214,group:境外媒体},{groupName:Twitter,num:0,group:Twitter},{groupName:FaceBook,num:0,group:FaceBook}]");
		}else if (typeCode.equals(ColumnConst.CHART_MAP)){ //地域
			result.setData("[{areaCount:6,areaName:山东,citys:[{areaCount:0,areaName:济南},{areaCount:6,areaName:青岛},{areaCount:0,areaName:淄博},{areaCount:0,areaName:枣庄},{areaCount:0,areaName:东营},{areaCount:0,areaName:烟台},{areaCount:0,areaName:潍坊},{areaCount:0,areaName:济宁},{areaCount:0,areaName:泰安},{areaCount:0,areaName:威海},{areaCount:0,areaName:日照},{areaCount:0,areaName:滨州},{areaCount:0,areaName:德州},{areaCount:0,areaName:聊城},{areaCount:0,areaName:临沂},{areaCount:0,areaName:菏泽},{areaCount:0,areaName:莱芜}]},{areaCount:1,areaName:福建,citys:[{areaCount:0,areaName:福州},{areaCount:1,areaName:厦门},{areaCount:0,areaName:漳州},{areaCount:0,areaName:泉州},{areaCount:0,areaName:三明},{areaCount:0,areaName:莆田},{areaCount:0,areaName:南平},{areaCount:0,areaName:龙岩},{areaCount:0,areaName:宁德},{areaCount:0,areaName:平潭县}]},{areaCount:0,areaName:台湾,citys:[{areaCount:0,areaName:台北},{areaCount:0,areaName:新北},{areaCount:0,areaName:台中},{areaCount:0,areaName:台南},{areaCount:0,areaName:高雄},{areaCount:0,areaName:宜兰县},{areaCount:0,areaName:新竹县},{areaCount:0,areaName:桃园县},{areaCount:0,areaName:苗栗县},{areaCount:0,areaName:彰化县},{areaCount:0,areaName:南投县},{areaCount:0,areaName:嘉义县},{areaCount:0,areaName:云林县},{areaCount:0,areaName:屏东县},{areaCount:0,areaName:台东县},{areaCount:0,areaName:花莲县},{areaCount:0,areaName:澎湖县},{areaCount:0,areaName:金门县},{areaCount:0,areaName:连江县},{areaCount:0,areaName:基隆},{areaCount:0,areaName:新竹},{areaCount:0,areaName:嘉义市}]},{areaCount:0,areaName:河北,citys:[{areaCount:0,areaName:石家庄},{areaCount:0,areaName:唐山市},{areaCount:0,areaName:秦皇岛},{areaCount:0,areaName:邯郸},{areaCount:0,areaName:邢台},{areaCount:0,areaName:保定},{areaCount:0,areaName:张家口},{areaCount:0,areaName:承德},{areaCount:0,areaName:沧州},{areaCount:0,areaName:廊坊},{areaCount:0,areaName:衡水}]},{areaCount:0,areaName:河南,citys:[{areaCount:0,areaName:郑州},{areaCount:0,areaName:开封},{areaCount:0,areaName:洛阳},{areaCount:0,areaName:平顶山},{areaCount:0,areaName:安阳},{areaCount:0,areaName:鹤壁},{areaCount:0,areaName:新乡},{areaCount:0,areaName:焦作},{areaCount:0,areaName:濮阳},{areaCount:0,areaName:许昌},{areaCount:0,areaName:漯河},{areaCount:0,areaName:三门峡},{areaCount:0,areaName:商丘},{areaCount:0,areaName:周口},{areaCount:0,areaName:驻马店},{areaCount:0,areaName:南阳},{areaCount:0,areaName:信阳},{areaCount:0,areaName:济源}]},{areaCount:0,areaName:重庆,citys:[{areaCount:0,areaName:渝中区},{areaCount:0,areaName:大渡口区},{areaCount:0,areaName:江北区},{areaCount:0,areaName:沙坪坝区},{areaCount:0,areaName:九龙坡区},{areaCount:0,areaName:南岸区},{areaCount:0,areaName:北碚区},{areaCount:0,areaName:渝北区},{areaCount:0,areaName:巴南区},{areaCount:0,areaName:涪陵区},{areaCount:0,areaName:綦江区},{areaCount:0,areaName:大足区},{areaCount:0,areaName:长寿区},{areaCount:0,areaName:江津区},{areaCount:0,areaName:合川区},{areaCount:0,areaName:永川区},{areaCount:0,areaName:南川区},{areaCount:0,areaName:璧山区},{areaCount:0,areaName:铜梁区},{areaCount:0,areaName:潼南区},{areaCount:0,areaName:荣昌区},{areaCount:0,areaName:万州区},{areaCount:0,areaName:梁平县},{areaCount:0,areaName:城口县},{areaCount:0,areaName:丰都县},{areaCount:0,areaName:垫江县},{areaCount:0,areaName:忠县},{areaCount:0,areaName:开县},{areaCount:0,areaName:云阳县},{areaCount:0,areaName:奉节县},{areaCount:0,areaName:巫山县},{areaCount:0,areaName:巫溪县},{areaCount:0,areaName:黔江区},{areaCount:0,areaName:武隆县},{areaCount:0,areaName:石柱土家族自治县},{areaCount:0,areaName:秀山土家族苗族自治县},{areaCount:0,areaName:酉阳土家族苗族自治县},{areaCount:0,areaName:彭水苗族土家族自治县}]},{areaCount:2,areaName:湖北,citys:[{areaCount:0,areaName:武汉},{areaCount:0,areaName:黄石},{areaCount:0,areaName:十堰},{areaCount:0,areaName:荆州},{areaCount:0,areaName:宜昌},{areaCount:0,areaName:襄阳},{areaCount:0,areaName:鄂州},{areaCount:0,areaName:荆门},{areaCount:0,areaName:黄冈},{areaCount:0,areaName:孝感},{areaCount:0,areaName:咸宁},{areaCount:0,areaName:随州},{areaCount:0,areaName:恩施土家族苗族自治州},{areaCount:0,areaName:仙桃},{areaCount:0,areaName:天门},{areaCount:0,areaName:潜江},{areaCount:0,areaName:神农架林区}]},{areaCount:0,areaName:湖南,citys:[{areaCount:0,areaName:长沙},{areaCount:0,areaName:株洲},{areaCount:0,areaName:湘潭},{areaCount:0,areaName:衡阳},{areaCount:0,areaName:邵阳},{areaCount:0,areaName:岳阳},{areaCount:0,areaName:常德},{areaCount:0,areaName:张家界},{areaCount:0,areaName:益阳},{areaCount:0,areaName:娄底},{areaCount:0,areaName:郴州},{areaCount:0,areaName:永州},{areaCount:0,areaName:怀化},{areaCount:0,areaName:湘西土家族苗族自治州}]},{areaCount:0,areaName:江西,citys:[{areaCount:0,areaName:南昌},{areaCount:0,areaName:九江},{areaCount:0,areaName:上饶},{areaCount:0,areaName:抚州},{areaCount:0,areaName:宜春},{areaCount:0,areaName:吉安},{areaCount:0,areaName:赣州},{areaCount:0,areaName:景德镇},{areaCount:0,areaName:萍乡},{areaCount:0,areaName:新余},{areaCount:0,areaName:鹰潭}]},{areaCount:0,areaName:海南,citys:[{areaCount:0,areaName:海口},{areaCount:0,areaName:三亚},{areaCount:0,areaName:三沙},{areaCount:0,areaName:五指山},{areaCount:0,areaName:琼海},{areaCount:0,areaName:儋州},{areaCount:0,areaName:文昌},{areaCount:0,areaName:万宁},{areaCount:0,areaName:东方},{areaCount:0,areaName:澄迈县},{areaCount:0,areaName:定安县},{areaCount:0,areaName:屯昌县},{areaCount:0,areaName:临高县},{areaCount:0,areaName:白沙黎族自治县},{areaCount:0,areaName:昌江黎族自治县},{areaCount:0,areaName:乐东黎族自治县},{areaCount:0,areaName:陵水黎族自治县},{areaCount:0,areaName:保亭黎族苗族自治县},{areaCount:0,areaName:琼中黎族苗族自治县},{areaCount:0,areaName:洋浦经济开发区}]},{areaCount:0,areaName:黑龙江,citys:[{areaCount:0,areaName:哈尔滨},{areaCount:0,areaName:齐齐哈尔},{areaCount:0,areaName:牡丹江},{areaCount:0,areaName:佳木斯},{areaCount:0,areaName:大庆},{areaCount:0,areaName:伊春},{areaCount:0,areaName:鸡西},{areaCount:0,areaName:鹤岗},{areaCount:0,areaName:双鸭山},{areaCount:0,areaName:七台河},{areaCount:0,areaName:绥化},{areaCount:0,areaName:黑河},{areaCount:0,areaName:大兴安岭地区}]},{areaCount:0,areaName:天津,citys:[{areaCount:0,areaName:东丽区},{areaCount:0,areaName:宝坻区},{areaCount:0,areaName:北辰区},{areaCount:0,areaName:滨海新区},{areaCount:0,areaName:和平区},{areaCount:0,areaName:河北区},{areaCount:0,areaName:河东区},{areaCount:0,areaName:河西区},{areaCount:0,areaName:红桥区},{areaCount:0,areaName:蓟县},{areaCount:0,areaName:津南区},{areaCount:0,areaName:静海区},{areaCount:0,areaName:南开区},{areaCount:0,areaName:宁河区},{areaCount:0,areaName:武清区},{areaCount:0,areaName:西青区}]},{areaCount:0,areaName:贵州,citys:[{areaCount:0,areaName:贵阳},{areaCount:0,areaName:六盘水},{areaCount:0,areaName:遵义},{areaCount:0,areaName:安顺},{areaCount:0,areaName:毕节},{areaCount:0,areaName:铜仁},{areaCount:0,areaName:黔西南布依族苗族自治州},{areaCount:0,areaName:黔东南苗族侗族自治州},{areaCount:0,areaName:黔南布依族苗族自治州}]},{areaCount:0,areaName:陕西,citys:[{areaCount:0,areaName:西安},{areaCount:0,areaName:宝鸡},{areaCount:0,areaName:咸阳},{areaCount:0,areaName:渭南},{areaCount:0,areaName:铜川},{areaCount:0,areaName:延安},{areaCount:0,areaName:榆林},{areaCount:0,areaName:安康},{areaCount:0,areaName:汉中},{areaCount:0,areaName:商洛}]},{areaCount:0,areaName:新疆,citys:[{areaCount:0,areaName:乌鲁木齐},{areaCount:0,areaName:克拉玛依},{areaCount:0,areaName:吐鲁番市},{areaCount:0,areaName:哈密地区},{areaCount:0,areaName:昌吉回族自治州},{areaCount:0,areaName:博尔塔拉蒙古自治州},{areaCount:0,areaName:巴音郭楞蒙古自治州},{areaCount:0,areaName:阿克苏地区},{areaCount:0,areaName:克孜勒苏柯尔克孜自治州},{areaCount:0,areaName:喀什地区},{areaCount:0,areaName:和田地区},{areaCount:0,areaName:伊犁哈萨克自治州},{areaCount:0,areaName:石河子},{areaCount:0,areaName:阿拉尔},{areaCount:0,areaName:图木舒克},{areaCount:0,areaName:五家渠},{areaCount:0,areaName:可克达拉},{areaCount:0,areaName:阿勒泰地区},{areaCount:0,areaName:塔城地区},{areaCount:0,areaName:双河},{areaCount:0,areaName:铁门关},{areaCount:0,areaName:北屯}]},{areaCount:0,areaName:澳门,citys:[{areaCount:0,areaName:澳门半岛},{areaCount:0,areaName:离岛},{areaCount:0,areaName:路氹城}]},{areaCount:1,areaName:江苏,citys:[{areaCount:0,areaName:南京},{areaCount:0,areaName:无锡},{areaCount:0,areaName:常州},{areaCount:0,areaName:苏州},{areaCount:0,areaName:南通市},{areaCount:0,areaName:连云港},{areaCount:0,areaName:淮安},{areaCount:0,areaName:盐城},{areaCount:0,areaName:扬州},{areaCount:0,areaName:镇江},{areaCount:0,areaName:泰州},{areaCount:0,areaName:宿迁}]},{areaCount:0,areaName:安徽,citys:[{areaCount:0,areaName:合肥},{areaCount:0,areaName:芜湖},{areaCount:0,areaName:蚌埠},{areaCount:0,areaName:淮南},{areaCount:0,areaName:马鞍山},{areaCount:0,areaName:淮北},{areaCount:0,areaName:铜陵},{areaCount:0,areaName:安庆},{areaCount:0,areaName:黄山},{areaCount:0,areaName:阜阳},{areaCount:0,areaName:宿州},{areaCount:0,areaName:滁州},{areaCount:0,areaName:六安},{areaCount:0,areaName:宣城},{areaCount:0,areaName:池州},{areaCount:0,areaName:毫州}]},{areaCount:0,areaName:西藏,citys:[{areaCount:0,areaName:拉萨},{areaCount:0,areaName:昌都},{areaCount:0,areaName:日喀则},{areaCount:0,areaName:林芝},{areaCount:0,areaName:山南地区},{areaCount:0,areaName:那曲地区},{areaCount:0,areaName:阿里地区}]},{areaCount:0,areaName:吉林,citys:[{areaCount:0,areaName:长春},{areaCount:0,areaName:吉林},{areaCount:0,areaName:四平},{areaCount:0,areaName:辽源},{areaCount:0,areaName:通化},{areaCount:0,areaName:白山},{areaCount:0,areaName:白城},{areaCount:0,areaName:松原},{areaCount:0,areaName:延边朝鲜族自治州},{areaCount:0,areaName:梅河口},{areaCount:0,areaName:公主岭}]},{areaCount:2,areaName:上海,citys:[{areaCount:0,areaName:黄浦区},{areaCount:0,areaName:浦东新区},{areaCount:0,areaName:徐汇区},{areaCount:0,areaName:长宁区},{areaCount:0,areaName:静安区},{areaCount:0,areaName:普陀区},{areaCount:0,areaName:闸北区},{areaCount:0,areaName:虹口区},{areaCount:0,areaName:杨浦区},{areaCount:0,areaName:闵行区},{areaCount:0,areaName:宝山区},{areaCount:0,areaName:嘉定区},{areaCount:0,areaName:金山区},{areaCount:0,areaName:松江区},{areaCount:0,areaName:青浦区},{areaCount:0,areaName:奉贤区},{areaCount:0,areaName:崇明县}]},{areaCount:1,areaName:山西,citys:[{areaCount:0,areaName:太原},{areaCount:0,areaName:大同},{areaCount:0,areaName:阳泉},{areaCount:0,areaName:长治},{areaCount:0,areaName:晋城},{areaCount:0,areaName:朔州},{areaCount:0,areaName:晋中},{areaCount:0,areaName:运城},{areaCount:0,areaName:忻州},{areaCount:1,areaName:临汾},{areaCount:0,areaName:吕梁}]},{areaCount:0,areaName:甘肃,citys:[{areaCount:0,areaName:兰州},{areaCount:0,areaName:嘉峪关},{areaCount:0,areaName:金昌},{areaCount:0,areaName:白银},{areaCount:0,areaName:天水},{areaCount:0,areaName:酒泉},{areaCount:0,areaName:张掖},{areaCount:0,areaName:武威},{areaCount:0,areaName:定西},{areaCount:0,areaName:陇南},{areaCount:0,areaName:平凉},{areaCount:0,areaName:庆阳},{areaCount:0,areaName:临夏州},{areaCount:0,areaName:甘南州}]},{areaCount:0,areaName:宁夏,citys:[{areaCount:0,areaName:银川},{areaCount:0,areaName:石嘴山},{areaCount:0,areaName:吴忠},{areaCount:0,areaName:固原},{areaCount:0,areaName:中卫}]},{areaCount:12,areaName:香港,citys:[{areaCount:6,areaName:香港岛},{areaCount:0,areaName:九龙半岛},{areaCount:0,areaName:新界}]},{areaCount:0,areaName:四川,citys:[{areaCount:0,areaName:成都},{areaCount:0,areaName:绵阳},{areaCount:0,areaName:自贡},{areaCount:0,areaName:攀枝花},{areaCount:0,areaName:泸州},{areaCount:0,areaName:德阳},{areaCount:0,areaName:广元},{areaCount:0,areaName:遂宁},{areaCount:0,areaName:内江},{areaCount:0,areaName:乐山},{areaCount:0,areaName:资阳},{areaCount:0,areaName:宜宾},{areaCount:0,areaName:南充},{areaCount:0,areaName:达州},{areaCount:0,areaName:雅安},{areaCount:0,areaName:阿坝藏族羌族自治州},{areaCount:0,areaName:甘孜藏族自治州},{areaCount:0,areaName:凉山彝族自治州},{areaCount:0,areaName:广安},{areaCount:0,areaName:巴中},{areaCount:0,areaName:眉山},{areaCount:0,areaName:彭州}]},{areaCount:1,areaName:浙江,citys:[{areaCount:1,areaName:杭州},{areaCount:0,areaName:宁波},{areaCount:0,areaName:温州},{areaCount:0,areaName:绍兴},{areaCount:0,areaName:湖州},{areaCount:0,areaName:嘉兴},{areaCount:0,areaName:金华},{areaCount:0,areaName:横州},{areaCount:0,areaName:台州},{areaCount:0,areaName:丽水},{areaCount:0,areaName:舟山}]},{areaCount:0,areaName:广西,citys:[{areaCount:0,areaName:南宁},{areaCount:0,areaName:柳州},{areaCount:0,areaName:桂林},{areaCount:0,areaName:梧州},{areaCount:0,areaName:北海},{areaCount:0,areaName:防城港},{areaCount:0,areaName:钦州},{areaCount:0,areaName:贵港},{areaCount:0,areaName:玉林},{areaCount:0,areaName:百色},{areaCount:0,areaName:贺州},{areaCount:0,areaName:河池},{areaCount:0,areaName:来宾},{areaCount:0,areaName:崇左}]},{areaCount:0,areaName:云南,citys:[{areaCount:0,areaName:昆明},{areaCount:0,areaName:曲靖},{areaCount:0,areaName:玉溪},{areaCount:0,areaName:保山},{areaCount:0,areaName:昭通},{areaCount:0,areaName:丽江},{areaCount:0,areaName:普洱},{areaCount:0,areaName:临沧},{areaCount:0,areaName:德宏傣族景颇族自治州},{areaCount:0,areaName:怒江僳僳族自治州},{areaCount:0,areaName:迪庆藏族自治州},{areaCount:0,areaName:大理白族自治州},{areaCount:0,areaName:楚雄彝族自治州},{areaCount:0,areaName:红河哈尼族彝族自治州},{areaCount:0,areaName:文山壮族苗族自治州},{areaCount:0,areaName:西双版纳傣族自治州}]},{areaCount:0,areaName:内蒙古,citys:[{areaCount:0,areaName:呼和浩特},{areaCount:0,areaName:包头},{areaCount:0,areaName:乌海},{areaCount:0,areaName:赤峰},{areaCount:0,areaName:通辽},{areaCount:0,areaName:鄂尔多斯},{areaCount:0,areaName:呼伦贝尔},{areaCount:0,areaName:巴彦淖尔},{areaCount:0,areaName:乌兰察布},{areaCount:0,areaName:兴安盟},{areaCount:0,areaName:锡林郭勒盟},{areaCount:0,areaName:阿拉善盟}]},{areaCount:0,areaName:辽宁,citys:[{areaCount:0,areaName:沈阳},{areaCount:0,areaName:大连},{areaCount:0,areaName:鞍山},{areaCount:0,areaName:抚顺},{areaCount:0,areaName:本溪},{areaCount:0,areaName:丹东},{areaCount:0,areaName:锦州},{areaCount:0,areaName:营口},{areaCount:0,areaName:阜新},{areaCount:0,areaName:辽阳},{areaCount:0,areaName:盘锦},{areaCount:0,areaName:铁岭},{areaCount:0,areaName:朝阳},{areaCount:0,areaName:葫芦岛}]},{areaCount:3,areaName:广东,citys:[{areaCount:0,areaName:广州},{areaCount:3,areaName:深圳},{areaCount:0,areaName:珠海},{areaCount:0,areaName:汕头},{areaCount:0,areaName:佛山},{areaCount:0,areaName:韶关},{areaCount:0,areaName:湛江},{areaCount:0,areaName:肇庆},{areaCount:0,areaName:江门},{areaCount:0,areaName:茂名},{areaCount:0,areaName:惠州},{areaCount:0,areaName:梅州},{areaCount:0,areaName:汕尾},{areaCount:0,areaName:河源},{areaCount:0,areaName:阳江},{areaCount:0,areaName:清远},{areaCount:0,areaName:东莞},{areaCount:0,areaName:中山},{areaCount:0,areaName:潮州},{areaCount:0,areaName:揭阳},{areaCount:0,areaName:云浮}]},{areaCount:0,areaName:青海,citys:[{areaCount:0,areaName:西宁},{areaCount:0,areaName:海东},{areaCount:0,areaName:海北藏族自治州},{areaCount:0,areaName:黄南藏族自治州},{areaCount:0,areaName:海南藏族自治州},{areaCount:0,areaName:果洛藏族自治州},{areaCount:0,areaName:玉树藏族自治州},{areaCount:0,areaName:海西蒙古族藏族自治州}]},{areaCount:2,areaName:北京,citys:[{areaCount:0,areaName:昌平区},{areaCount:0,areaName:朝阳区},{areaCount:0,areaName:大兴区},{areaCount:0,areaName:东城区},{areaCount:0,areaName:西城区},{areaCount:0,areaName:房山区},{areaCount:0,areaName:丰台区},{areaCount:0,areaName:海淀区},{areaCount:0,areaName:怀柔区},{areaCount:0,areaName:门头沟区},{areaCount:0,areaName:石景山区},{areaCount:0,areaName:平谷区},{areaCount:0,areaName:顺义区},{areaCount:0,areaName:通州区},{areaCount:0,areaName:密云县},{areaCount:0,areaName:延庆县}]");
		}else if (typeCode.equals(ColumnConst.CHART_WORD_CLOUD)){ //词云
			result.setData("{groupList:[{fieldValue:印度,count:433,entityType:location},{fieldValue:中国,count:391,entityType:location},{fieldValue:甘地,count:332,entityType:people},{fieldValue:美国,count:208,entityType:location},{fieldValue:北京市,count:183,entityType:location},{fieldValue:亚洲,count:152,entityType:location},{fieldValue:英国,count:148,entityType:location},{fieldValue:非洲,count:145,entityType:location},{fieldValue:习近平,count:138,entityType:people},{fieldValue:英迪拉·甘地,count:131,entityType:people},{fieldValue:孟买,count:119,entityType:location},{fieldValue:莫迪,count:113,entityType:people},{fieldValue:马云,count:111,entityType:people},{fieldValue:查尔斯·穆里甘地,count:109,entityType:people},{fieldValue:卢旺达,count:109,entityType:location},{fieldValue:卢旺达大学,count:109,entityType:agency},{fieldValue:中非,count:106,entityType:location},{fieldValue:卢旺达大学孔子学院,count:106,entityType:agency},{fieldValue:穆克什·安巴尼,count:105,entityType:people},{fieldValue:穆克什,count:96,entityType:people},{fieldValue:德国,count:95,entityType:location},{fieldValue:也门,count:90,entityType:location},{fieldValue:卢比,count:88,entityType:people},{fieldValue:伦敦,count:87,entityType:location},{fieldValue:印度政府,count:85,entityType:agency},{fieldValue:洛克菲勒,count:84,entityType:people},{fieldValue:博斯克·卡加巴,count:82,entityType:people},{fieldValue:拉胡尔·甘地,count:82,entityType:people},{fieldValue:法国,count:82,entityType:location},{fieldValue:日本,count:82,entityType:location},{fieldValue:卢旺达政策分析研究所,count:82,entityType:agency},{fieldValue:圣雄甘地,count:81,entityType:people},{fieldValue:瓦西,count:80,entityType:people},{fieldValue:让·恩塔基,count:80,entityType:people},{fieldValue:迈克·乌威玆耶,count:80,entityType:people},{fieldValue:米蕾,count:80,entityType:people},{fieldValue:卡加梅,count:80,entityType:people},{fieldValue:穆黑,count:80,entityType:people},{fieldValue:大湖,count:80,entityType:location},{fieldValue:万村,count:80,entityType:location},{fieldValue:大湖地区,count:80,entityType:location},{fieldValue:中国医疗队,count:80,entityType:agency},{fieldValue:卢旺达马萨卡医院,count:80,entityType:agency},{fieldValue:卢旺达农业发展局技术员米蒂爱特里斯·哈图吉马纳,count:80,entityType:agency},{fieldValue:卢旺达广播电台,count:80,entityType:agency},{fieldValue:欧洲,count:79,entityType:location},{fieldValue:腾讯,count:78,entityType:agency},{fieldValue:纽约,count:72,entityType:location},{fieldValue:李锋,count:70,entityType:people},{fieldValue:万宇,count:70,entityType:people}]}");
		}
		return result;
	}

}
