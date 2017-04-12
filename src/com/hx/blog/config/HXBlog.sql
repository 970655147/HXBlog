/**************/
/*            */
/* HXBlog.sql */
/*            */
/**************/

/* Disable Foreign Keys */
pragma foreign_keys = off;
/* Begin Transaction */
begin transaction;

/* Drop table [main].[blogList] */
drop table if exists [main].[blogList];

/* Table structure [main].[blogList] */
CREATE TABLE [main].[blogList] (
  [id] integer, 
  [path] varchar(256), 
  [tag] varchar(64), 
  [createTime] varchar(32), 
  [good] integer, 
  [notGood] integer, 
  [visited] integer, 
  [commentsNum] INTEGER DEFAULT 0);

/* Data [main].[blogList] */
insert into [main].[blogList] values(8, '2015-12-25_21-56-41-604__dateBlogSep__java锁的种类以及辨析(一) 自旋锁', 'java,lock', '2015-12-25 21 : 56', 0, 0, 5, 0);
insert into [main].[blogList] values(9, '2015-12-25_21-58-01-365__dateBlogSep__Java锁的种类以及辨析(二) 自旋锁的其他种类', 'java,lock', '2015-12-25 21 : 58', 0, 1, 3, 1);
insert into [main].[blogList] values(10, '2015-12-25_21-58-47-476__dateBlogSep__Java锁的种类以及辨析(三) 阻塞锁', 'java,lock', '2015-12-25 21 : 58', 0, 0, 2, 0);
insert into [main].[blogList] values(11, '2015-12-25_21-59-38-367__dateBlogSep__Java锁的种类以及辨析(四) 可重入锁', 'java,lock', '2015-12-25 21 : 59', 0, 0, 2, 0);
insert into [main].[blogList] values(13, '2015-12-28_16-02-12-202__dateBlogSep__eclipse C & CPP编译含有多个main函数的项目', 'eclipse,cpp,mingw', '2015-12-28 16 : 02', 1, 0, 5, 1);
insert into [main].[blogList] values(14, '2016-01-31_20-12-12-282__dateBlogSep__自动机字符串匹配', '字符串匹配,java', '2016-01-31 20 : 12', 0, 1, 4, 1);
insert into [main].[blogList] values(15, '2016-01-31_20-33-32-863__dateBlogSep__KMP 算法', '字符串匹配,java', '2016-01-31 20 : 33', 1, 1, 8, 3);
insert into [main].[blogList] values(16, '2016-01-31_21-10-18-950__dateBlogSep__Boyer-Moore 算法', '字符串匹配,java', '2016-01-31 21 : 10', 1, 1, 35, 9);


/* Drop table [main].[commentList] */
drop table if exists [main].[commentList];

/* Table structure [main].[commentList] */
CREATE TABLE [main].[commentList] (
  [id] integer, 
  [blogIdx] integer, 
  [floorIdx] integer, 
  [commentIdx] integer, 
  [name] varchar(64), 
  [email] varchar(64), 
  [headImgIdx] integer, 
  [date] varchar(64), 
  [toUser] varchar(64), 
  [privilege] varchar(64), 
  [comment] varchar(256));

/* Data [main].[commentList] */
insert into [main].[commentList] values(54, 15, 1, 1, '970655147@qq.com', '970655147@qq.com', 3, '2016-02-19 15 : 51', '970655', 'guest', 'SendRequest/_Post');
insert into [main].[commentList] values(55, 15, 2, 1, '970655147@qq.com', '970655147@qq.com', 3, '2016-02-19 15 : 57', '970655', 'guest', 'SendRequest/_PostBy/_Node.js/_Post');
insert into [main].[commentList] values(56, 16, 1, 1, '蓝风970655147', '970655147@qq.com', 1, '2016-03-20 13 : 44', '970655', 'admin', 'Hello World !');
insert into [main].[commentList] values(57, 9, 1, 1, '蓝风970655147', '970655147@qq.com', 0, '2016-03-20 14 : 35', '970655', 'admin', 'good');
insert into [main].[commentList] values(58, 16, 2, 1, '970655', '970655111@qq.com', 6, '2016-03-20 14 : 41', '970655', 'guest', 'dfgh');
insert into [main].[commentList] values(59, 15, 3, 1, '970655', '970655111@qq.com', 8, '2016-03-20 14 : 41', '970655', 'guest', 'TestTest');
insert into [main].[commentList] values(60, 14, 1, 1, '970655', '970655111@qq.com', 0, '2016-03-20 14 : 42', '970655', 'guest', '好搓啊');
insert into [main].[commentList] values(61, 13, 1, 1, '970655', '970655111@qq.com', 0, '2016-03-20 14 : 42', '970655', 'guest', '真好, 评论一个');
insert into [main].[commentList] values(66, 16, 7, 1, '970655', '970655147@qq.com', 8, '2016-05-01 14 : 35', '970655', 'guest', '/&lt;script/&gt; alert/("msg05"/) /&lt;//script/&gt;
');
insert into [main].[commentList] values(67, 16, 8, 1, 'buhaobuhao', 'buhaobuhao@163.com', 3, '2016-07-30 21 : 06', '970655', 'guest', '发表于 2016.07.30');
insert into [main].[commentList] values(68, 16, 8, 2, 'buhaobuhao', 'buhaobuhao@163.com', 3, '2016-07-30 21 : 07', 'buhaobuhao', 'guest', 'document.write/("Hello World"/)');
insert into [main].[commentList] values(69, 16, 5, 1, 'sdf', 'sdfsdfsdf@126.com', 8, '2016-07-30 21 : 17', 'buhaobuhao', 'guest', ' this is a comment');
insert into [main].[commentList] values(70, 16, 8, 3, 'sdf', 'sdfsdfsdf@126.com', 0, '2016-07-30 21 : 18', 'buhaobuhao', 'guest', 'jawa');
insert into [main].[commentList] values(71, 16, 7, 2, 'sdf', 'sdfsdfsdf@126.com', 0, '2016-07-30 21 : 19', '970655', 'guest', '11111');
insert into [main].[commentList] values(72, 16, 7, 3, 'sdf', 'sdfsdfsdf@126.com', 0, '2016-07-30 21 : 20', 'sdf', 'guest', '你特么是废话');
insert into [main].[commentList] values(73, 16, 9, 1, 'sdf', 'sdfsdfsdf@126.com', 9, '2016-07-30 21 : 23', '970655', 'guest', 'newComment');
insert into [main].[commentList] values(74, 15, 2, 2, 'sdf', 'sdfsdfsdf@126.com', 0, '2016-07-30 21 : 24', '970655147@qq.com', 'guest', 'test');
insert into [main].[commentList] values(75, 16, 3, 1, '970655', '970655111@qq.com', 0, '2016-03-20 14 : 42', '970655', 'guest', '3l');
insert into [main].[commentList] values(77, 16, 4, 1, '970655', '970655111@qq.com', 0, '2016-03-20 14 : 42', '970655', 'guest', '4l, 水军的干活');
insert into [main].[commentList] values(78, 16, 6, 1, '970655', '970655111@qq.com', 0, '2016-03-20 14 : 42', '970655', 'guest', '6l, 小弟小弟
');


/* Commit Transaction */
commit transaction;

/* Enable Foreign Keys */
pragma foreign_keys = on;
