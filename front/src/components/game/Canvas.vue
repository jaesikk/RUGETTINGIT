<template>
  <div id="body container">
    <div class="row">
      <div class="col controls-colors-wrapper">
        <div id="jsColors" class="controls-colors">
          <ColorPicker v-model="ctxcolor" />
          <div class="jsColor" style="background-color: #2c2c2c;"></div>
          <div class="jsColor" style="background-color: white;"></div>
          <div class="jsColor" style="background-color: #ff3b30;"></div>
          <div class="jsColor" style="background-color: #ff9500;"></div>
          <div class="jsColor" style="background-color: #fc0;"></div>
          <div class="jsColor" style="background-color: #4cd963;"></div>
          <div class="jsColor" style="background-color: #5ac8fa;"></div>
          <div class="jsColor" style="background-color: #0579ff;"></div>
          <div class="jsColor" style="background-color: #5856d6;"></div>
        </div>
      </div>
      <div class="col">
        <canvas
          @mousemove="onMouseMove"
          @mousedown="startPainting"
          @mouseup="stopPainting"
          @mouseleave="stopPainting"
          @click="handleCanvasClick"
          @contextmenu="handleCM"
          id="jsCanvas"
          class="canvas"
        ></canvas>
        <div class="controls-range">
          <input
            @input="handleRangeChange"
            type="range"
            id="jsRange"
            min="0.1"
            max="15.0"
            value="7.5"
            step="0.1"
          />
        </div>
        <div>
          <div class="controls-btn-box">
            <div class="controls-btns">
              <!-- <button
                @click="handleModeClick"
                id="jsMode"
              >
                Fill
              </button> -->
              <button @click="handlePaintClick" :class="{ picked : mode_painting }">
                <i class="pi pi-pencil"></i>
                <span>Paint</span>
              </button>
              <button @click="handleFillClick" :class="{ picked : mode_filling }">
                <i class="pi pi-circle-on"></i>
                <span>Fill</span>
              </button>
              <button @click="handleEraseClick" :class="{ picked : mode_erasing }" id="jsEraser">
                <i class="pi pi-calendar"></i>
                <span>Eraser</span>
              </button>
              <button @click="handleClearClick" id="jsClear">
                <i class="pi pi-trash"></i>
                <span>Clear</span>
              </button>
              <Button @click="handleSaveClick" id="jsSave" icon="pi pi-save" label="Save"></Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// import axios from 'axios'
import 'primevue/resources/primevue.min.css'
const INITIAL_COLOR = "#2c2c2c"
const CANVAS_WIDTH = 800
const CANVAS_HEIGHT = 500


export default {
  name: 'Canvas',
  data: function() {
    return {
      canvas : undefined,
      ctx : undefined,
      colors : undefined,
      range : undefined,
      saveBtn : undefined,

      // 기본  ctx컬러
      ctxcolor : "#2c2c2c",

      painting : false,
      mode_painting : true,
      mode_filling : false,
      mode_erasing : false,
    }
  },
  methods: {
    // painting을 시작했을 때
    startPainting: function () {
      this.painting = true
    },
    // painting을 멈췄을 때
    stopPainting: function () {
      this.painting = false
    },
    onMouseMove: function (event) {
      // mode_filling 모드인 상태에서 클릭말고 드래그 방지
      if (this.mode_filling === false) {
        const x = event.offsetX
        const y = event.offsetY
        if (!this.painting) {
          this.ctx.beginPath()
          this.ctx.moveTo(x, y)
        } else {
          this.ctx.lineTo(x, y)
          this.ctx.stroke()
        }
      }
    },
    // Color 클릭
    handleColorClick: function (event) {
      // console.log(event.target)
      const color = event.target.style.backgroundColor
      this.ctx.strokeStyle = color
      this.ctx.fillStyle = color
    },
    // ColorPicker 클릭
    handleColorPickerClick: function () {
      this.ctx.strokeStyle = '#' + this.ctxcolor
      this.ctx.fillStyle = this.ctxcolor
    },
    handleRangeChange: function (event) {
      const size = event.target.value
      this.ctx.lineWidth = size
    },
    handlePaintClick: function () {
      //mode_filling, mode_erasing이 true이면 둘 다 false로 바꿔줘야함
      if (this.mode_filling === true || this.mode_erasing === true) {
        this.mode_filling = false
        this.mode_erasing = false
      }
      this.ctx.globalCompositeOperation='source-over'
      this.mode_painting = true
    },
    handleFillClick: function () {
      if (this.mode_painting === true || this.mode_erasing === true) {
        this.mode_painting = false
        this.mode_erasing = false
      }
      this.ctx.globalCompositeOperation='source-over'
      this.mode_filling = true
    },
    handleEraseClick: function () {
      // fill은 true인 상태로 들어온다면
      if (this.mode_painting === true || this.mode_filling === true) {
        this.mode_painting = false
        this.mode_filling = false
      }
      this.ctx.globalCompositeOperation='destination-out'
      this.mode_erasing = true
    },
    handleClearClick: function () {
      this.ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)
    },
    handleCanvasClick: function () {
      if (this.mode_filling) {
        this.ctx.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)
      }
    },
    // 마우스 우클릭 방지
    handleCM: function (event) {
      event.preventDefault()
    },
    handleSaveClick: function () {
      const image = this.canvas.toDataURL('image/png')
      const link = document.createElement("a")
      link.href = image

      // 일단 save 버튼 누를시에 spring으로 전달하는 것
      // console.log(this.canvas)
      // console.log(image)
      // console.log(link)

      link.download = "kons example"
      // fake click
      link.click()
    },
  },
  watch: {
    ctxcolor() {
      this.handleColorPickerClick()
    }
  },
  mounted: function() {
    this.canvas = document.getElementById("jsCanvas")
    this.ctx = this.canvas.getContext("2d")
    this.colors = document.getElementsByClassName("jsColor")
    // color 선택 이벤트
    if (this.colors) {
      Array.from(this.colors).forEach(color => color.addEventListener("click", this.handleColorClick))
    }
    // pixel modifier
    this.canvas.width = CANVAS_WIDTH
    this.canvas.height = CANVAS_HEIGHT
    // default 설정
    this.ctx.fillStyle = "white"
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height)
    this.ctx.strokeStyle = INITIAL_COLOR
    this.ctx.fillStyle = INITIAL_COLOR
    this.ctx.lineWidth = 7.5
  },
  unmounted: function () {
    const image = this.canvas.toDataURL('image/png')
    let bstr = atob(image.split(",")[1])
    let n = bstr.length
    let u8arr = new Uint8Array(n)

    while(n--) {
      u8arr[n] = bstr.charCodeAt(n)
    }

    let today = new Date()

    let year = today.getFullYear()
    let month = today.getMonth() + 1
    let date = today.getDate()
    let hours = today.getHours(); // 시
    let minutes = today.getMinutes();  // 분
    let seconds = today.getSeconds();  // 초
    const rand_0_9 = Math.floor(Math.random() * 100)

    let imgname = year + '-' + month + '-' + date + '-' + hours + '-' + minutes + '-' + seconds + this.$store.state.nickname + rand_0_9

    const file = new File([u8arr], imgname, {type:"mine"})
    console.log(file)
    // let form = new FormData()
    // form.append('file', file)

    this.$store.dispatch('setFile', file)
    // 현재 컨버스는 좌표를 따고 있지 않습니다.
    // 완성본만을 따고 있습니다. 그래서 그리는 족족은 불가
    // 따라서 지금 현재 unmounted 될 때 store에 백으로 보낼수 있는 data의 형태를 갖춰서 저장해 놓고
    // GameResult가 mounte 될 때 axios로 보내주겠다.
  }
}
</script>

<style>
/* @import "../../reset.css"; */
#body {
  /* background-color: #f6f9fc; */
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 2rem;
}

.canvas {
  width: 800px;
  height: 500px;
  background-color: white;
  border-radius: 0 15px 15px 15px;
  box-shadow: 0 5px 10px rgb(50 50 93 / 11%), 0 5px 10px rgb(0 0 0 / 8%);
}

/* 래핑 */
.p-colorpicker, .p-colorpicker-overlay {
  width: 30px !important;
  height: 30px !important;
  border-radius: 25px !important;
  margin: 10px;
  -webkit-box-shadow: 5px 5px 15px 1px #FF8080, -9px 5px 15px 1px #FFE488, -7px -5px 15px 1px #8CFF85, 12px -5px 15px 1px #80C7FF, 2px 2px 2px 2px #E488FF, 5px 5px 18px -4px rgba(34,255,54,0);
  box-shadow: 5px 5px 15px 1px #FF8080, -9px 5px 15px 1px #FFE488, -7px -5px 15px 1px #8CFF85, 12px -5px 15px 1px #80C7FF, 2px 2px 2px 2px #E488FF, 5px 5px 18px -4px rgba(34,255,54,0);}
/* 실제 컬러피커 프리뷰 */
.p-colorpicker-preview {
  border-radius: 25px !important;
  width: 30px !important;
  height: 30px !important;
}

.col {
  padding: 0 !important;
}

.controls-colors {
  margin: 0px 0px 0px 5px;
  padding: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  border-radius: 10px 0 0 10px;
  box-shadow: 0.2rem 0.2rem 0.5rem #cacaca;
}

.controls-colors > .jsColor {
  margin: 5px;
  width: 30px !important;
  height: 30px !important;
  border-radius: 25px;
  cursor: pointer;
  box-shadow: 0.2rem 0.2rem 0.5rem #cacaca;
}


.controls-btn-box {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.controls-btns {
  margin-bottom: 5px;
}

.controls-btns button {
  all: unset;
  cursor: pointer;
  background-color: white;
  padding: 5px 0px;
  width: 80px;
  text-align: center;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgb(50 50 93 / 11%), 0 1px 3px rgb(0 0 0 / 8%);
  border: 2px solid rgba(0, 0, 0, 0.2);
  color: solid rgba(0, 0, 0, 0.8);
  text-transform: uppercase;
  font-weight: 600;
  font-size: 12px;
  margin: 5px;
}

.controls-btns button:active {
  transform: scale(0.98);
}


.controls-range {
  margin-top: 10px;
  margin-bottom: 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.controls-btns .picked {
  all: unset;
  cursor: pointer;
  background-color: #F75E82;
  padding: 5px 0px;
  width: 80px;
  text-align: center;
  border-radius: 10px;
  box-shadow: 0 4px 6px rgb(50 50 93 / 11%), 0 1px 3px rgb(0 0 0 / 8%);

  border: 2px solid rgba(0, 0, 0, 0.2);
  color: solid rgba(0, 0, 0, 0.8);
  text-transform: uppercase;
  font-weight: 600;
  font-size: 12px;
}

.p-button:hover {
  background-color: #F75E82 !important;
  border-color: #c64b68 !important;
}
.controls-btns > button, .p-button {
  box-sizing: border-box !important;
  display: inline-flex !important;
  justify-content: space-evenly !important;
  align-items: flex-end !important;
  padding-right: 3px !important;
}

.p-button-icon-left {
  margin: 0 !important;
}
.p-button-label {
  flex: none !important;
}


</style>
