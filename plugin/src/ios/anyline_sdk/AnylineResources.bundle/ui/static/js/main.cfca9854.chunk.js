(window.webpackJsonp=window.webpackJsonp||[]).push([[0],{226:function(t,e,n){t.exports=n(470)},254:function(t,e){},276:function(t,e){},278:function(t,e){},452:function(t,e){},466:function(t,e,n){},468:function(t,e,n){},470:function(t,e,n){"use strict";n.r(e);var o,i=n(0),r=n.n(i),a=n(47),c=n.n(a),s=n(8),u=n(13),l=n(2),d=n(24),p=n.n(d),f=n(216),h=n.n(f),b=n(3),v=n(219),m=n.n(v),g=n(25),O=n(472);var k=function(){if(o)return o;var t={},e=function(e,n){return t[e]?t[e].next(n):(console.warn("".concat(e," does not have any registered listeners. Publish cancelled.")),!1)};return o={getSinks:function(){return t},publish:e,subscribe:function(e,n){return n?(t[e]||Object.assign(t,Object(g.a)({},e,new O.a)),t[e].subscribe(n)):(console.warn("No callback provided for subscription. Subscription cancelled."),!1)}},window.publish=e,o}(),y=function(t,e){return t.reduce(function(t,n){return Object(l.a)({},t,Object(g.a)({},n[e],n))},{})},j=n(75),w=n(220),C=n.n(w),E=n(16),x=n(9),F=n(6);function S(t){var e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:0,n=parseInt(t.slice(0,2),16),o=parseInt(t.slice(2,4),16),i=parseInt(t.slice(4,6),16);return"rgba(".concat(n,", ").concat(o,", ").concat(i,", ").concat(e,")")}function I(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:"";if(t=t.replace("#",""),![6,8].includes(t.length))throw Error("invalid hey HexARGB string use length of 6 or 8");if(6===t.length){var e=parseInt(t.substring(0,2),16),n=parseInt(t.substring(2,4),16),o=parseInt(t.substring(4,6),16);return"rgb(".concat(e,", ").concat(n,", ").concat(o,")")}if(8===t.length){var i=parseInt(t.substring(0,2),16)/255,r=parseInt(t.substring(2,4),16),a=parseInt(t.substring(4,6),16),c=parseInt(t.substring(6,8),16);return"rgba(".concat(r,", ").concat(a,", ").concat(c,", ").concat(i,")")}}var R=function(t){return t.strokeWidth||2},_=function(t){return I(t.strokeColor||"AA0099FF")},M=function(t){return I(t.fillColor||"00000000")},W=function(t){return"".concat(R(t),"px solid ").concat(_(t))};function A(t){var e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:2,n=[];t=t.filter(function(){return!0});for(var o=0;o<t.length;o+=1){var i=o+1>t.length-1?(o+1)%t.length:o+1,r=o+2>t.length-1?(o+2)%t.length:o+2,a=t[o],c=t[i],s=t[r],u=Math.sqrt(Math.pow(a.x-c.x,2)+Math.pow(a.y-c.y,2)),l=(u-e)/u,d=[((1-l)*a.x+l*c.x).toFixed(1),((1-l)*a.y+l*c.y).toFixed(1)],p=e/Math.sqrt(Math.pow(c.x-s.x,2)+Math.pow(c.y-s.y,2)),f=[((1-p)*c.x+p*s.x).toFixed(1),((1-p)*c.y+p*s.y).toFixed(1)];o===t.length-1&&n.unshift("M".concat(f.join(","))),n.push("L".concat(d.join(","))),n.push("Q".concat(c.x,",").concat(c.y,",").concat(f.join(",")))}return n.push("Z"),n.join(" ")}var T=s.a.svg.withConfig({displayName:"Polygon__Svg",componentId:"sc-1ns3j6n-0"})(["",""],function(t){return"\n    position: fixed;\n    top: ".concat(t.svgOffset.top,"px;\n    left: ").concat(t.svgOffset.left,"px;\n    width: 100vw;\n    height: 100vh;\n    stroke: ").concat(_(t),";\n    stroke-width: ").concat(R(t),";\n    fill: ").concat(M(t),";\n  ")}),P=Object(b.a)(Object(b.g)(function(t){var e=t.points,n=Object(j.a)(t,["points"]),o=parseInt(function(t){var e=t.cornerRadius;return"".concat(e||0,"px")}(n),10);return{pathComponent:o?r.a.createElement("path",{d:A(e.map(function(t){var e=Object(u.a)(t,2);return{x:e[0],y:e[1]}}),o)}):r.a.createElement("polyline",{points:[].concat(Object(E.a)(e),[e[0]]).map(function(t){var e=Object(u.a)(t,2),n=e[0],o=e[1];return"".concat(n,",").concat(o)}).join(" ")})}}))(function(t){var e=t.svgOffset,n=t.pathComponent,o=t.strokeColor,i=t.strokeWidth,a=t.fillColor;return r.a.createElement(T,{svgOffset:e,strokeColor:o,fillColor:a,strokeWidth:i},n)});var L,N=s.a.div.withConfig({displayName:"ScanFeedback__Rect",componentId:"bhwmdw-0"})(["position:absolute;transition:top ",",left ",",width ",",height ",";",";"],"0.0s","0.0s","0.0s","0.0s",function(t){return"\n    \n    border: ".concat(R(t),"px solid transparent;\n\n    width: ").concat(t.initialStyle.width,"px;\n    height: ").concat(t.initialStyle.height,"px;\n    top: ").concat(t.initialStyle.top,"px;\n    left: ").concat(t.initialStyle.left,"px;\n    border-radius: ").concat(t.initialStyle.borderRadius,"px;\n\n\n    ").concat(t.show&&"\n      border: ".concat(W(t),";\n      background-color: ").concat(M(t),";\n    "),";\n    \n  ")}),U=s.a.div.withConfig({displayName:"ScanFeedback__Wrapper",componentId:"bhwmdw-1"})(["",";"],function(t){return"\n    position: absolute;\n    transition: top ".concat("0.0s",", left ").concat("0.0s",", width ").concat("0.0s",", height ").concat("0.0s",";\n    background-clip: content-box;\n    border: ").concat(W(t),";\n\n    ").concat("rect"===t.feedbackStyle&&"\n      border: ".concat(R(t),"px solid transparent;\n    "),";\n  \n    ").concat("contour_rect"===t.feedbackStyle&&"\n      background-color: ".concat(M(t),";\n      border-radius: ").concat(t.cornerRadius,"px;\n\n      ").concat(t.points&&"\n        border: ".concat(R(t),"px solid transparent;\n      "),"\n    "),";\n\n    ").concat("contour_underline"===t.feedbackStyle&&"\n      transform-origin: top;\n      border-top: none;\n      top: ".concat(t.y+t.height,"px !important;\n      height: 0px !important;\n      width: ").concat(t.width,"px !important;\n\n      ").concat(t.points&&"\n        left: ".concat(t.x,"px !important;\n      "),"\n\n      background-color: ").concat(_(t),";\n    "),";\n\n    ").concat("contour_point"===t.feedbackStyle&&"\n      border-radius: 50%;\n      top: ".concat(t.y+t.height,"px !important; \n      left: ").concat(t.x+t.width/2,"px !important;\n      height: 0px !important;\n      width: 0px !important;\n    "),";\n  ")}),z=Object(i.forwardRef)(function(t,e){var n=Object(F.a)({},t);return r.a.createElement(U,Object.assign({ref:e},n,function(t,e){if(!t)return{};var n=t.reduce(function(t,e){var n=Object(u.a)(e,2),o=n[0],i=n[1];return{x1:t.x1&&t.x1<o?t.x1:o,y1:t.y1&&t.y1<i?t.y1:i,x2:t.x2&&t.x2>o?t.x2:o,y2:t.y2&&t.y2>i?t.y2:i}},{});return{x:n.x1-e.left,y:n.y1-e.top,width:n.x2-n.x1,height:n.y2-n.y1}}(n.points,n.polygonOffset)),n.points&&("contour_rect"===n.feedbackStyle||"rect"===n.feedbackStyle)&&r.a.createElement(P,n))}),D={transition:function(t){var e=t.duration;return{duration:(void 0===e?1e3:e)/t.elements.length,type:"keyframes",values:[0,1,1,0],times:[0,.01,.99,1]}},opacity:1},B={transition:function(t){t.duration,t.elements;return{duration:300,type:"keyframes",values:[0,1,0],times:[0,.5,1]}},opacity:1},Z={transition:function(t){t.duration,t.elements;return{duration:300,type:"keyframes",values:[0,1,0,0],times:[0,.25,.3,1]}},opacity:1},K={transition:function(t){var e=t.duration,n=void 0===e?1e3:e,o=t.elements,i=t.strokeWidth;return{duration:n/o.length,type:"keyframes",values:[1,(R({strokeWidth:i})+3)/R({strokeWidth:i}),1],times:[0,.5,1]}},scaleY:1},V={transition:function(t){var e=t.duration,n=void 0===e?1e3:e,o=t.elements,i=t.strokeWidth;return{duration:n/o.length,type:"keyframes",values:[1,(R({strokeWidth:i})+3)/R({strokeWidth:i}),1],times:[0,.5,1]}},scale:1},q={TRAVERSE_MULTI:{parent:{active:{staggerChildren:function(t){var e=t.children,n=t.duration;return(void 0===n?1e3:n)/(2*e.length)}},reverse:{},inactive:{}},child:{active:Object(l.a)({},{transition:function(t){var e=t.duration;return{duration:(void 0===e?1e3:e)/t.elements.length,type:"keyframes",values:[0,1,0],times:[0,.5,1]}},opacity:1}),reverse:{opacity:0},inactive:{opacity:0}}},TRAVERSE_SINGLE:{parent:{active:{staggerChildren:100},reverse:{},inactive:{}},child:{active:Object(l.a)({},D),reverse:{opacity:0},inactive:{opacity:0}}},KITT:{parent:{active:{staggerChildren:50},reverse:{staggerChildren:50,staggerDirection:-1},inactive:{}},child:{active:Object(l.a)({},D),reverse:Object(l.a)({},D),inactive:{opacity:0}}},BLINK:{parent:{active:{},reverse:{},inactive:{}},child:{active:Object(l.a)({},B),reverse:Object(l.a)({},B),inactive:{opacity:0}}},RESIZE:{underline:{parent:{active:{staggerChildren:function(t){var e=t.children,n=t.duration;return(void 0===n?1e3:n)/(2*e.length)}},reverse:{},inactive:{}},child:{active:Object(l.a)({},K),reverse:{opacity:1},inactive:{opacity:1}}},point:{parent:{active:{staggerChildren:function(t){var e=t.children,n=t.duration;return(void 0===n?1e3:n)/(2*e.length)}},reverse:{},inactive:{}},child:{active:Object(l.a)({},V),reverse:{opacity:1},inactive:{opacity:1}}}},PULSE:{parent:{active:{delayChildren:function(t){return t.firstRun?0:600}},reverse:{delayChildren:600},inactive:{}},child:{active:Object(l.a)({},Z),reverse:Object(l.a)({},Z),inactive:{opacity:0}}}},G=Object(x.a)(z)(Object(l.a)({},q.RESIZE.point.child)),H=x.a.div(Object(l.a)({},q.RESIZE.point.parent)),J=Object(x.a)(z)(Object(l.a)({},q.RESIZE.underline.child)),Q=x.a.div(Object(l.a)({},q.RESIZE.underline.parent)),Y=Object(x.a)(z)(Object(l.a)({},q.TRAVERSE_MULTI.child)),$=x.a.div(Object(l.a)({},q.TRAVERSE_MULTI.parent)),X=Object(x.a)(z)(Object(l.a)({},q.TRAVERSE_SINGLE.child)),tt=x.a.div(Object(l.a)({},q.TRAVERSE_SINGLE.parent)),et=Object(x.a)(z)(Object(l.a)({},q.PULSE.child)),nt=x.a.div(Object(l.a)({},q.PULSE.parent)),ot=Object(x.a)(z)(Object(l.a)({},q.BLINK.child)),it=x.a.div(Object(l.a)({},q.BLINK.parent)),rt=Object(x.a)(z)(Object(l.a)({},q.KITT.child)),at={none:[function(t){var e=t.children;return r.a.createElement("div",null,e)},z],kitt:[x.a.div(Object(l.a)({},q.KITT.parent)),rt],blink:[it,ot],pulse:[nt,et],traverse_multi:[$,Y],traverse_single:[tt,X],resize_point:[H,G],resize_underline:[Q,J],resize:[]},ct=s.a.div.withConfig({displayName:"ScanFeedbackManager__Wrapper",componentId:"sc-1d2gw37-0"})([""]),st=function(t){if(!t.length)return{};var e=t[0],n=e.y,o=e.x;return{width:e.width,height:e.height,top:n,left:o}},ut=Object(b.a)(Object(b.b)({config:{feedbackStyle:"",animation:"",elements:[]}}),Object(b.g)(function(t){var e=t.config;return{config:Object(l.a)({},e,{elements:e.elements||[],feedbackStyle:e.feedbackStyle&&e.feedbackStyle.toLowerCase(),animation:e.animation&&e.animation.toLowerCase()}),renderRect:"rect"===e.feedbackStyle.toLowerCase()&&e.elements.length&&e.elements[0]&&!e.elements[0].points}}),Object(b.h)("feedbackElements","setFeedbackElements",[]),Object(b.h)("nextFeedbackElements","setNextFeedbackElements",[]),Object(b.h)("pose","setPose",null),Object(b.h)("firstRun","setFirstRun",!0),Object(b.f)({handlePoseComplete:function(t){var e=t.config,n=t.setPose,o=t.setFirstRun,i=t.setFeedbackElements;return function(t){L&&(L=!1,i(e.elements)),n("reverse"!==t?"reverse":"active"),o(!1)}}}),Object(b.c)({componentDidMount:function(){var t=this.props,e=t.config,n=t.setPose;(e.animation||"none"!==e.animation)&&(this.props.setFeedbackElements(e.elements),n("active"))},componentWillReceiveProps:function(t){if(this.props.config.lastFeedbackUpdate!==t.config.lastFeedbackUpdate)if(t.pose||this.props.setPose("active"),t.renderRect||!t.config.animation||"none"===t.config.animation||["rect","contour_rect"].includes(t.config.feedbackStyle)||t.config.elements.length>=this.props.config.elements.length)this.props.setFeedbackElements(t.config.elements);else{L=!0;var e=this.props.config.elements.slice(t.config.elements.length-this.props.config.elements.length).map(function(t){return Object(l.a)({},t,{hidden:!0})});this.props.setFeedbackElements([].concat(Object(E.a)(t.config.elements),Object(E.a)(e)))}}}))(function(t){var e=t.config,n=t.pose,o=t.handlePoseComplete,i=t.firstRun,a=t.initialRectStyle,c=t.cutoutId,s=t.feedbackElements,l=t.hide,d=t.polygonOffset,p=t.renderRect,f=function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:{},e=t.feedbackStyle,n=void 0===e?"rect":e,o=t.animation,i=void 0===o?"none":o;return at[i]?["rect","contour_rect"].includes(n)?at.none:"contour_point"===n&&"resize"===i?at.resize_point:"contour_underline"===n&&"resize"===i?at.resize_underline:at[i]:(console.warn("animation ".concat(i," not found!")),at.none)}(e),h=Object(u.a)(f,2),b=h[0],v=h[1];return r.a.createElement(ct,{style:{display:l?"none":"block"}},p?r.a.createElement(N,{show:s.length,style:st(s),initialStyle:a}):r.a.createElement(b,{firstRun:i,onPoseComplete:o,duration:e.animationDuration,pose:n},s.map(function(t,n){return r.a.createElement(v,Object.assign({style:{top:t.y,left:t.x,width:t.width,height:t.height,visibility:t.hidden?"hidden":"visible"},key:"".concat(c,"_").concat(n)},e,t,{polygonOffset:d}))})))});var lt=function(){var t;return function(e,n){clearTimeout(t),t=setTimeout(n,e)}}(),dt=s.a.div.withConfig({displayName:"Cutout__Wrapper",componentId:"sc-1fys6on-0"})(["position:fixed;border-style:solid;",""],function(t){return"\n        width: ".concat(t.rect.right-t.rect.left,"px;\n        height: ").concat(t.rect.bottom-t.rect.top,"px;\n        top: ").concat(t.rect.top-t.strokeWidth,"px;\n        left: ").concat(t.rect.left-t.strokeWidth,"px;\n        border-radius: ").concat(t.cornerRadius,"px;\n        border-width: ").concat(t.strokeWidth,"px;\n        border-color: ").concat(t.scanning?I(t.feedbackStrokeColor||t.strokeColor):I(t.strokeColor),";\n    ")}),pt=function(){return{fade:500,zoom:500}[(arguments.length>0&&void 0!==arguments[0]?arguments[0]:"").toLowerCase()]||0},ft=Object(b.a)(Object(b.h)("show","setShow"),Object(b.h)("scanning","setScanning"),Object(b.b)({rect:{left:0,top:0,right:100,bottom:100},strokeWidth:2,strokeColor:"FFFFFF",cornerRadius:8}),Object(b.g)(function(t){var e=t.scanFeedback;return{renderFeedback:e.elements&&!!e.elements.length}}),Object(b.f)({resetScanTimeout:function(t){var e=t.setScanning;return function(){e(!0),lt(700,function(){return e(!1)})}}}),Object(b.c)({componentDidMount:function(){this.props.setShow(!0)},componentWillReceiveProps:function(t){t.scanFeedback.lastFeedbackUpdate!==this.props.scanFeedback.lastFeedbackUpdate&&this.props.resetScanTimeout()}}))(function(t){t.show;var e,n,o,i=t.renderFeedback,a=t.scanFeedback,c=Object(j.a)(t,["show","renderFeedback","scanFeedback"]);return r.a.createElement(C.a,Object.assign({timeout:pt(c.classNames)},c),r.a.createElement(dt,c,i&&r.a.createElement(ut,{initialRectStyle:(e=c.rect,n=c.strokeWidth,o=c.cornerRadius,{width:e.right-e.left,height:e.bottom-e.top,top:-n,left:-n,borderRadius:o}),hide:!c.scanning,cutoutId:c.id,polygonOffset:{top:c.rect.top,left:c.rect.left},config:a})))}),ht=n(223),bt=function(t,e,n){var o=Math.min(t,e)/2;return o<n?o:n};n(224);var vt=n(122),mt=n.n(vt),gt=n(225),Ot=function(t){return new Promise(function(e){return setTimeout(e,t)})};function kt(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:[],e=Object(E.a)(t);return{getFeedbacks:function(){return e},set:function(t){return e=Object(E.a)(t)},add:function(t){return e=[].concat(Object(E.a)(e),[t])}}}function yt(){return(yt=Object(gt.a)(mt.a.mark(function t(){var e,n,o,i,r,a,c,s,u,d,p,f,h=arguments;return mt.a.wrap(function(t){for(;;)switch(t.prev=t.next){case 0:e=h.length>0&&void 0!==h[0]?h[0]:{},n=e.setupCutoutsConfig,o=void 0===n?{}:n,i=e.feedbackCount,r=void 0===i?8:i,window.publish("setupMask",{outerColor:"000000",outerAlpha:.3}),window.publish("setupCutouts",[Object(l.a)({scanFeedback:{animation:"RESIZE",redrawTimeout:0,fillColor:"220099FF",strokeWidth:2,strokeColor:"FF0099FF",cornerRadius:4,feedbackStyle:"contour_point",animationDuration:1e3},feedbackStrokeColor:"FF0099FF",id:"1",visible:1,animation:"none",rect:{bottom:210,top:110,left:150,right:250},strokeWidth:2,cornerRadius:20,strokeColor:"FFFFFFFF"},o)]),function(t,e){return Object(l.a)({},t,e)},function(t){return t.slice(-1)[0]},a=function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:0,e=arguments.length>1&&void 0!==arguments[1]?arguments[1]:100;return Math.floor(Math.random()*e)+t},c=Array.apply(void 0,Object(E.a)(Array(r))).map(function(t,e){return{y:250,x:a(1,1200),width:14.351851267873542,height:20.833332485622833}}),s=c.map(function(t){return Object(l.a)({},t,{y:252})}),u=s.slice(-2),Array.apply(void 0,Object(E.a)(Array(r-4))).map(function(t,e){return{y:200,x:a(1,1200),width:14.351851267873542,height:20.833332485622833}}),d=kt(c),p=0;case 12:if(!(p<=100)){t.next=22;break}return console.time("loop"),f=d.set(p%2?u:c),window.publish("updateFeedback",{1:f}),console.timeEnd("loop"),t.next=19,Ot(30);case 19:p+=1,t.next=12;break;case 22:case"end":return t.stop()}},t,this)}))).apply(this,arguments)}window.runFeedbackBenchmark=function(){return yt.apply(this,arguments)};var jt,wt,Ct,Et,xt,Ft,St=function(t,e){return Object.keys(e).filter(function(e){return!Object.keys(t).includes(e)})},It=s.a.canvas.withConfig({displayName:"Mask__Canvas",componentId:"sc-5i53g0-0"})(["position:absolute;width:100%;height:100%;"]),Rt=function(t){return function(e){return t.set("canvas",e)}},_t=Object(b.a)(ht.withRefs,Object(b.b)({outerColor:"FFFFFF",outerAlpha:0,onInit:function(){}}),Object(b.f)({initializeMaskService:function(t){var e=t.outerColor,n=t.outerAlpha,o=t.refs.canvas;return function(){o.width=o.offsetWidth,o.height=o.offsetHeight,jt=function(t){var e=t.canvasEl,n=t.outerColor,o=void 0===n?"000000":n,i=t.outerAlpha,r=void 0===i?0:i,a=t.rerenderOnAdd,c=void 0===a||a,s=t.rerenderOnRemove,u=void 0===s||s,l={},d=S(o,r),p=e.getContext("2d");function f(t){var e=t.rect,n=t.cornerRadius,o=void 0===n?0:n,i=t.strokeWidth,r=void 0===i?0:i;o<r&&(o=r);var a=e.top,c=e.left,s=e.right-c,u=e.bottom-a,l=c,d=a;o=bt(s,u,o),p.beginPath(),p.moveTo(l+o,d-r),p.lineTo(l+s-o+r,d-r),p.arc(l+s-o+r,d+o-r,o,1.5*Math.PI,0),p.lineTo(l+s+r,d+u-o+r),p.arc(l+s-o+r,d+u-o+r,o,0,.5*Math.PI),p.lineTo(l+o-r,d+u+r),p.arc(l+o-r,d+u-o+r,o,.5*Math.PI,Math.PI),p.lineTo(l-r,d+o-r),p.arc(l+o-r,d+o-r,o,Math.PI,1.5*Math.PI),p.closePath(),p.fillStyle="black",p.fill()}function h(t){p.fillStyle=t,p.fillRect(0,0,e.width,e.height)}function b(){p.clearRect(0,0,e.width,e.height),p.globalCompositeOperation="source-over",h(d),p.globalCompositeOperation="destination-out",Object.values(l).forEach(f)}return h(d),function t(){return requestAnimationFrame(t)}(),{addCutout:function(t){l[t.id]=t,c&&b()},modifyCutout:function(t){l[t.id]=t,b()},removeCutout:function(t){delete l[t],u&&b()},render:b,setConfig:function(t,e){d=S(t,e),b()}}}({canvasEl:o,outerColor:e,outerAlpha:n,rerenderOnAdd:!1})}},updateCanvasDimensions:function(t){var e=t.refs.canvas;return function(){e.width=e.offsetWidth,e.height=e.offsetHeight,jt&&jt.render()}}}),Object(b.c)({componentDidMount:function(){this.props.initializeMaskService(),window.addEventListener("resize",this.props.updateCanvasDimensions),this.props.onInit(jt)},componentWillUnmount:function(){window.removeEventListener("resize",this.props.updateCanvasDimensions)},componentWillReceiveProps:function(t){var e=this;return t.outerColor===this.props.outerColor&&t.outerAlpha===this.props.outerAlpha||jt&&jt.setConfig(t.outerColor,t.outerAlpha),St(this.props.cutouts,t.cutouts).length?(jt||t.initializeMaskService(),void Object.values(t.cutouts).filter(function(t){return!e.props.cutouts[t.id]}).forEach(jt.addCutout)):St(t.cutouts,this.props.cutouts).length?(jt||t.initializeMaskService(),void Object.values(this.props.cutouts).filter(function(e){return!t.cutouts[e.id]}).map(function(t){return t.id}).forEach(jt.removeCutout)):void Object.values(t.cutouts).filter(function(t){return e.props.cutouts[t.id]&&t.lastUpdate!==e.props.cutouts[t.id].lastUpdate}).forEach(jt.modifyCutout)}}))(function(t){var e=t.refs;return r.a.createElement(It,{ref:Rt(e)})}),Mt=(n(466),s.a.div.withConfig({displayName:"CutoutManager__Wrapper",componentId:"ri6bt1-0"})(["width:100%;height:100%;"])),Wt={cutouts:p.a.object.isRequired},At=function(t){return t.visible},Tt=function(t,e){return e.map(function(e){return Object(l.a)({},e,{svgOffset:{top:t.useSvgOffset?-t.rect.top:0,left:t.useSvgOffset?-t.rect.left:0},x:void 0===e.x?void 0:e.x-t.rect.left-(t.scanFeedback.strokeWidth||2),y:void 0===e.y?void 0:e.y-t.rect.top-(t.scanFeedback.strokeWidth||2)})})},Pt=function(){var t=arguments.length>0&&void 0!==arguments[0]?arguments[0]:function(t){return t};return Object(b.a)(Object(b.h)("maskConfig","setMaskConfig"),Object(b.h)("cutouts","setCutouts",{}),Object(b.f)({handleAnimatonEnterStart:function(t){var e=t.cutouts;return function(t){var n=t.id,o=e[n];o.animation&&"none"!==o.animation.toLowerCase()||Ft.render()}},handleAnimatonEnterEnd:function(){return function(t){t.id,Ft.render()}},handleAnimatonExitEnd:function(){return function(t){t.id}},handleMaskInit:function(){return function(t){Ft=t}},setupCutouts:function(t){var e=t.setCutouts,n=t.cutouts;return function(t){var o=t.map(function(t){return Object(l.a)({},n[t.id]||{},t,{scanFeedback:Object(l.a)({},n[t.id]&&n[t.id].scanFeedback||{},t.scanFeedback||{},{elements:[]}),lastUpdate:Date.now(),visible:!!t.visible})}),i=y(o,"id");e(function(t){return Object(l.a)({},t,i)})}},updateFeedback:function(t){var e=t.setCutouts,n=t.cutouts;return function(t){var o=Object.entries(t).filter(function(t){var e=Object(u.a)(t,1)[0];return n[e]}).map(function(t){var e=Object(u.a)(t,2),o=e[0],i=e[1];return Object(l.a)({},n[o],{scanFeedback:Object(l.a)({},n[o].scanFeedback,{lastFeedbackUpdate:Date.now(),elements:Tt(n[o],i)})})}),i=y(o,"id");e(function(t){return Object(l.a)({},t,i)})}},removeCutouts:function(t){var e=t.setCutouts,n=t.cutouts;return function(t){var o=function(t,e){var n=e.map(function(t){return t.toString()});return Object.entries(t).reduce(function(t,e){var o=Object(u.a)(e,2),i=o[0],r=o[1];return n.includes(i)?t:Object(l.a)({},t,Object(g.a)({},i,r))},{})}(n,t);e(o)}}}),t,Object(b.g)(function(t){var e=t.cutouts,n=Object.values(e).filter(At);return{cutouts:y(n,"id")}}),Object(b.c)({componentDidMount:function(){wt=k.subscribe("setupCutouts",this.props.setupCutouts),Et=k.subscribe("updateFeedback",h()(this.props.updateFeedback,40)),Ct=k.subscribe("removeCutouts",this.props.removeCutouts),xt=k.subscribe("setupMask",this.props.setMaskConfig)},componentWillUnmount:function(){wt.unsubscribe(),Ct.unsubscribe(),Et.unsubscribe(),xt.unsubscribe()}}),Object(b.d)(Wt))}()(function(t){var e=t.cutouts,n=t.maskConfig,o=t.handleMaskInit,i=t.handleAnimatonEnterEnd,a=t.handleAnimatonExitEnd,c=t.handleAnimatonEnterStart;return r.a.createElement(Mt,null,r.a.createElement(_t,Object.assign({},n,{cutouts:e,onInit:o})),r.a.createElement(m.a,null,Object.values(e).map(function(t){return r.a.createElement(ft,Object.assign({onEntered:i,onEnter:c,onExited:a,classNames:t.animation?t.animation.toLowerCase():"",key:"Cutout_".concat(t.id)},t))})))}),Lt=s.a.div.withConfig({displayName:"App__Wrapper",componentId:"sc-1wz7824-0"})(["width:100vw;height:100vh;"]),Nt=function(){return r.a.createElement(Lt,null,r.a.createElement(Pt,null))};Boolean("localhost"===window.location.hostname||"[::1]"===window.location.hostname||window.location.hostname.match(/^127(?:\.(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)){3}$/));n(468);c.a.render(r.a.createElement(Nt,null),document.getElementById("root")),"serviceWorker"in navigator&&navigator.serviceWorker.ready.then(function(t){t.unregister()})}},[[226,2,1]]]);
//# sourceMappingURL=main.cfca9854.chunk.js.map