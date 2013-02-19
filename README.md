WeiboFetcher
============

WeiboFetcher是一个用于抓取指定新浪微博账号中全部微博的小工具。
因为WeiboFetcher模拟了浏览器的操作，所以它需要一个有效的Cookie才能获取数据。
Cookie可由Fiddler2探测获取，或者通过Google Chrome自带的chrome://net-internals得到。
2013.02.19 测试有效。

### USAGE

在命令行下输入 java -jar WeiboFetcher.jar %指定账号的UID% %Cookie% %选项%

UID：UID为账户主页地址中http://weibo.com/UID或者http://weibo.com/u/UID。

Cookie：Cookie的格式为"xxx=xxx; xxx=xxx; xxx=xxx"（包含双引号）。例如："myuid=???; SINAGLOBAL=???; un=???; wvr=5; __utma=???; __utmz=???; SSOLoginState=???; USRUG=???; _s_tentry=???; Apache=???; ULV=???; UOR=???; WBStore=???; USRHAWB=???; SUE=???; SUP=???; SUS=???; ALF=???; v=5"

选项：-o FILENAME或--output FILENAME指定输出的文件名，否则默认为data.xml；-d DELAY或--delay DELAY设定每次抓取整页微博时延迟的毫秒数，默认1000。


### LICENSE

THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

Components：
* [WeiboFetcher](http://github.com/skies457/weibo-fetcher/) licensed under MIT. See LICENSE.mit
* [HttpClient](http://hc.apache.org/httpcomponents-client-ga/) from Apache licunsed under Apache License v2. See LICENSE.apache
* [HtmlParser](http://htmlparser.sourceforge.net/) licensed under CPLv1. See LICENSE.cpl
* [JSONSimple](http://code.google.com/p/json-simple/) licensed under Apache License v2. See LICENSE.apache


### CONTACT US

Any question please send an email to mystery.wd#qq.com
Thanks for looking up the water meter but we have it installed inside :).
