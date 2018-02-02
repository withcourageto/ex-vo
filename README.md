**VO 规则说明**

本篇文章介绍本系统VO 的使用

### 注意：在开发完成之后，发现 Jackson 可以实现大部分的功能，但是，仍有部分需求 Jackson 不能满足，例如 @VOFieldMapping 进行字段值映射，@VOFields进行字段多重映射

### 为什么使用VO

本地系统使用 restfulApi 作为接口风格，所以返回前端的数据所有都是 json 格式的内容，本系统的前端内容转换采用spring默认的jackson；

又因为本系统采用hibernate 作为数据层，所以构建了大量实体类，这些实体类和前端需要的数据并不是一一对应， 例如状态字段：

status: 1 ,前端需要status,statusText 两个字段，后端数据库只有一个status 字段，这就造成不匹配，

为了简化前端的开发工作，我们决定在后端把数据处理好了再返回前端，于是VO类的需求产生了，但是为每一个api构建 VO类将耗费大量工作，并且

Vo规则的改变不可避免会导致实体到VO的映射代码修改，所以本系统决定采用动态生成Vo类的方式，将已有的实体类，通过注解，按照一定规则动态生成新的VO类。


注意： 在开发之后，发现Jackson 可以实现大部分的功能，但是，仍有部分需求jackson不能满足，最重要的功能是使用 @VOFieldMapping 进行字段值映射，@VOFields进行字段多重映射


### 怎么使用VO

1. 使用注解标识需要转换VO对象的实体类，在其类上标识 @VOType 注解，或者在其字段（只支持字段）上标注 @VOField, @VOFields, @VOFieldMapping 注解
2. 使用 VOUtil.modelToView() 方法生成VO对象；

'''

    1.  Using VoType
    @VOType
    class User{

        private String userName;
        private int age;
        private Date birthday;
        // getter and setter
    }

    example:

    User user = new User();
    user.userName = "root";
    user.age = 20;
    user.birthday = new Date();
    Object obj = VOUtil.modelToView(user); // obj -> {"userName":"root", "age":20, "birthday": 200023432002 }


    2. Using VoField
    class Apply{

        @VoField("applyId")
        private int id;

        private int user;

        @VoField("status")
        @VoFieldMapping(value ="statusText", rule="[0:'processing',1:'pass', 2:'reject']")
        private int status;
    }

    example:

    Apply apply = new Apply();
    apply.id = 1;
    apply.user = 20;
    apply.status = 1;
    Object obj = VOUtil.modelToView(apply); // obj -> {"id":1, "user":20, "status": 1, "statusText": "pass"}, 同时返回 status 和 statusText
'''

### 规则

* @VOType
  1. ModelToViewPolicy.ALL_FIELD 默认值：所有字段都会进行Vo转换
  2. ModelToViewPolicy.WITHIN_VO_FIELD ： 只转换使用 @VOField, @VOFields, @VOFieldMapping 标准的字段
* @VOField
    指定转换细节： 如果指定name, 转换结果名称使用name的值，如果没有指定，转换名称使用字段名称
                 如果指定script, 将会对script 进行求值，转换结果的值将会是script的值
                 如果 recursion为true, 将会对自定义类型的字段 进行递归转换，否则直接返回原始值
* @VOFieldMapping 指定属性映射，根据字段的值决定返回值，主要，用于 代码和名称的转换 ，例如： gender , status 字段可以使用
* @VOFields : 用于设置多个 @VOField，将一个值转换为多个VO值

### 涉及类
*   cn.net.comsys.xuegong.wechat.base.anno.VOType
*   cn.net.comsys.xuegong.wechat.base.anno.VOField
*   cn.net.comsys.xuegong.wechat.base.anno.VOFields
*   cn.net.comsys.xuegong.wechat.base.anno.VOFieldMapping
*   cn.net.comsys.xuegong.wechat.util.VOUtil

### 注意事项
*   必须调用 VOUtil.modelToView 方法
*   转换过程会去掉 Vo注解，和 javax.persistence包下的注解，转换过程会去掉类注解（将来版本解决）
*   转换过程依据字段，不依据属性，也就是说父类的方法不能被获取
*   请保证实体类的getter, setter 与字段名称一致（非常重要，将来解决此问题）

