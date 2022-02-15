# 手写Rpc框架

核心：

一个服务就是一个对象的一个方法
不同的方法对应不同的服务

## 项目结构

多模块管理

+ 协议模块
+ 序列化模块
+ 网络模块
+ Server模块
+ Client模块
+ 通用模块（一些通用的工具方法）

![QQ截图20220215165543](D:\项目实战\javaweb项目\smallrpc\image\QQ截图20220215165543.jpg)

+ 协议模块（描述Server和Client之间的通信协议）

  + ServiceDescriptor 代表了一个服务的描述信息

  + Request 表示了需要请求的服务以及服务所要携带的一些参数
  + Response 表示了Server返回给Client的返回信息，返回是否成功，返回值等等

+ 序列化模块（对象与二进制数据的互转）
  + 就是比较常规的Decoder和Encoder
+ 网络模块（具体的网络通信模块）
  + 这里主要是做了基于HTTP的实现

+ Server模块
  + RpcServer
  + ServiceManager 维护RpcServer需要暴露出去的服务（注册功能）
  + ServiceInstance 表示了暴露出去的服务的具体对象
+ Client模块
  + RpcClient
  + RemoteInvoker 会把客户端的请求通过RemoteInvoker和服务器做交互，交互信息是通过Request和Response来封装的。
  + TransportSelector 客户端和服务器做连接时，可以是一个客户端连接一个服务器，也可以是一个客户端连接多个服务器，所以做了Selector层抽象。

## 协议模块

+ Peer 定义了网络传输的一个端点
+ ServiceDescriptor 代表了一个服务的描述信息
  + 包含类名、方法名、返回类型、参数类型[]
+ Request 中
  + 包含一个ServiceDescriptor以及一组参数
+ Response
  + 包含服务返回编码，信息和具体的返回数据

## 通用模块

+ 反射的工具类ReflectionUtils
  + 根据类创建对象`newInstance(Class<T> clazz)`
  + 获取某个类所有的公共方法`getPublicMethods(Class clazz)`
  + 调用某个对象指定方法 `invoke(Object object,Method method,Object... args)`

## 序列化模块(基于JSON)

+ Encoder接口
+ Decoder接口
  + 泛型`<T> T decode(byte[] bytes,Class<T> clazz) `

## 网络模块

+ TransportClient(Interface)

  > 1.创建连接
  >
  > 2.发送数据
  >
  > 3.关闭连接

  + `void connect(Peer peer)`
  + `InputStream write(InputStream data)`
  + `void close()`

+ TransportServer(Interface)

  > 1.启动 监听端口
  >
  > 2.接受请求
  >
  > 3.关闭监听

  + `void init(int port,RequestHandler handler)`在这里将RequestHandler传进去

  + `void start()`
  + `void stop()`

+ RequestHandler(Interface)处理网络请求的Handler 上面的TransportServer拿到的是数据流,这个是在它之上的一层封装,用来处理具体的请求
  + `onRequest(InputStream recive,OutputStream toResp)`

下面是HTTP的具体实现

+ HTTPTransportClient
  + 需要url

+ HTTPTransportServer
  + Server是基于Jetty的 将Jetty.Server new出来

## Server模块

+ RpcServerConfig server配置
  + TranportServer 多态 可以换不同的网络层
  + Encoder Encoder 多态 可以换不同的序列化器
  + port 端口号

+ ServiceInstance 表示一个具体服务
  + Object Methon 谁的什么方法
+ ServiceManager 管理rpc暴露的服务(注册服务和查找服务)
  + `Map<ServiceDescriptor ,ServiceInstance> services = new ConcurrentHashMap<>()`
  + 注册`register(Class<T> interfaceClass,T bean)`前者是类后者是该类具体实现的对象,这个对象后面写成单例
  + 查找`lookup(Request request)`

+ ServiceInvoker 调用具体服务

+ RpcServer

  + RpcServcerConfig

  + TransportServer net

  + Encoder Devoder

  + ServiceManager

  + ServiceInvoker

  + 利用反射将RpcConfig中的类赋进来

  + `register()`

  + `start()`

  + `stop()`

  + `RequestHandler handler`

    > 1. 从InputStream中读取request数据体
    > 2. 调用invoker执行
    > 3. 将返回值写入OutputSteam即可

```java
private RequestHandler requestHandler = new RequestHandler() {
        public void onRequest(InputStream recive, OutputStream toResp) {

            Response resp = new Response();

            try {
                byte[] inBytes = IOUtils.readFully(recive,recive.available());
                Request request = decoder.decode(inBytes,Request.class);
                log.info("get request: {}",request);

                ServiceInstance sis = serviceManager.lookup(request);
                Object ret = serviceInvoker.invoke(sis,request);
                resp.setData(ret);



            } catch (Exception e) {
                log.warn(e.getMessage(),e);
                resp.setCode(1);
                resp.setMessage("RpcServer got error"
                        +e.getClass().getName()+" : "
                        +e.getMessage());
            }finally {
                try {
                    byte[] outBytes = encoder.encode(resp);
                    toResp.write(outBytes);
                    log.info("response client");
                } catch (IOException e) {
                    log.warn(e.getMessage(),e);
                }
            }
        }
    };
```

## Client模块

+ TransportSelector 表示选择哪个server去连接(Server选择的路由类)

  + `void init(List<Peer> peers,int count,Class<? extends TransportClient> clazz)`

    初始化selector

    peers可以连接的server端点信息

    count client与server建立多少个连接

    clazz client实现class

    

  + `TransportClient select()`选择一个transport与server做交互

  + `void relese(TransportClient client)`释放用完的client

  + `void close()`关闭所有的客户端连接对象

这里有个客户端连接对象连接池的概念,`List<TranportClient>`,针对每个peer,初始化时建立count这么多的客户端连接对象,并放入连接池中.

将上述的所有的方法加上synchronized,来实现线程安全

+ RpcClientConfig(和ServerConfig大同小异)
+ RpcClient
  + 同样利用反射将RpcClientConfig中的配置进行赋值
  + `public <T> T getProxy(Class<T> clazz)`返回接口的代理对象,利用动态代理`RemoteInvoker implements InvocationHandler  `

动态代理的知识:

[java动态代理实现与原理详细分析 - Gonjian - 博客园 (cnblogs.com)](https://www.cnblogs.com/gonjan-blog/p/6685611.html)

+ RemoteInvoker
  + invoke方法(这个才是像调用本地方法一样调用远程方法的**核心**

```java
public Object invoke(Object proxy,
                         Method method,
                         Object[] args) throws Throwable {
        Request request = new Request();
        request.setService(ServiceDescriptor.from(clazz,method));
        request.setParameters(args);

        Response resp = invokeRemote(request);
        if (resp == null||resp.getCode()!=0){
            throw new IllegalStateException("fail to invoke remote: "+resp);
        }
        return resp.getData();

    }

    private Response invokeRemote(Request request) {
        TransportClient client = null;
        Response resp = null;
        try {
            client = selector.select();
            byte[] outBytes = encoder.encode(request);
            InputStream revice = client.write(new ByteArrayInputStream(outBytes));

            byte[] inBytes =  IOUtils.readFully(revice,revice.available());
            resp = decoder.decode(inBytes,Response.class);

        } catch (IOException e) {
            log.warn(e.getMessage(),e);
            resp = new Response();
            resp.setCode(1);
            resp.setMessage("RpcClient got error:"+
                            e.getClass()+
                            ":"+e.getMessage());
        } finally {
            if (client!=null)
                selector.release(client);
        }
        return resp;
    }
```

## 框架使用

1. 针对自身的Server和Client引入对应的包

   ```
   <dependencies>
           <dependency>
               <groupId>com.gscsd</groupId>
               <artifactId>small-rpc-client</artifactId>
               <version>1.0-SNAPSHOT</version>
           </dependency>
           <dependency>
               <groupId>com.gscsd</groupId>
               <artifactId>small-rpc-server</artifactId>
               <version>1.0-SNAPSHOT</version>
           </dependency>
       </dependencies>
   ```

2. Client

   ```
   public class Client {
       public static void main(String[] args) {
           RpcClient client = new RpcClient();
           CalcService service = client.getProxy(CalcService.class);//获取远程服务代理对象
   
           int r1 =  service.add(1,2);//执行对应的方法
           int r2 =  service.minus(10,8);
   
           System.out.println(r1);
           System.out.println(r2);
       }
   }
   ```

2. Server

   ```
   public class Server {
   
       public static void main(String[] args) {
           RpcServer server = new RpcServer();
           server.register(CalcService.class,new CalcServiceImpl());//注册服务
           server.start();//开启服务器
       }
   }
   ```

3. 具体服务 这里是一个简单的加法服务

