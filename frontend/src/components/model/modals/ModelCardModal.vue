<template lang="html">
  <div class="w3-modal" draggable="false">
    <div class="w3-modal-content">
      <div class="w3-card-4 app-modal-card w3-display-container">

        <div class="w3-display-topright">
          <div class="w3-right w3-button close-btn w3-xlarge" @click="closeThisConfirm()">
            <i class="fa fa-times" aria-hidden="true"></i>
          </div>
          <div class="w3-right">
            <app-model-modal-buttons
              v-if="isLoggedAnEditor && !editing"
              :show="showEditButtons"
              @edit="startEditing()"
              @delete="deleteCard()"
              @remove="removeCard()"
              :in-section-title="inSectionTitle">
            </app-model-modal-buttons>
          </div>
        </div>

        <div class="w3-container w3-border-bottom">
          <h3>{{ isNew ? 'New Card' : 'Model Card'}}</h3>
        </div>

        <div v-if="!loading" class="div-modal-content w3-container">

          <div v-if="isNew" class="w3-row">
            <label class=""><b>In Section:</b></label>
            <br>
            <h4>{{ this.inSectionTitle }}</h4>
          </div>

          <div v-if="isNew" class="section-tabs w3-row w3-center light-grey">
            <div class="w3-col s6 w3-bottombar w3-hover-light-grey cursor-pointer"
              :class="{'border-blue-app': !addExisting}"
              @click="addExisting = false">
              <h5 class="noselect" :class="{'bold-text': !addExisting}">Create New</h5>
            </div>
            <div class="w3-col s6 w3-bottombar w3-hover-light-grey cursor-pointer"
              :class="{'border-blue-app': addExisting}"
              @click="addExisting = true; existingCard = null">
              <h5 class="noselect" :class="{'bold-text': addExisting}">Add Existing</h5>
            </div>
          </div>

          <div class="slider-container">
            <transition name="slideLeftRight">
              <div v-if="addExisting && editing" class="">
                <div class="w3-row">
                  <app-model-card-selector
                    :inSectionId="inSectionId"
                    @select="cardSelected($event)">
                  </app-model-card-selector>
                  <app-error-panel
                    :show="noCardSelectedShow"
                    message="please select one card from above">
                  </app-error-panel>
                </div>

                <div v-if="existingCard !== null" class="w3-row w3-container w3-margin-top">
                  <label class="">
                    <b>New scope (in "{{ inSectionTitle }}" section):</b>
                  </label>
                  <select v-model="existingCardNewScope"
                    class="w3-input w3-topbar" :class="selectorExistingBorderClass" name="">
                    <option value="PRIVATE">Private (only I can see it)</option>
                    <option value="SHARED">Shared (others can see it, but its mine)</option>
                    <option value="COMMON">Common (controlled by all editors)</option>
                  </select>
                </div>

              </div>
            </transition>
          </div>

          <div :class="{'slider-container': animatingTab}">
            <transition name="slideRightLeft"
              @before-enter="animatingTab = true"
              @after-enter="animatingTab = false"
              @before-leave="animatingTab = true"
              @after-leave="animatingTab = false">

              <div v-if="!addExisting">

                <div v-if="editing && canChangeScope" class="w3-row w3-margin-top">
                  <label class="">
                    <b>Scope (in "{{ inSectionTitle }}" section):</b>
                  </label>
                  <select v-model="editedCard.newScope"
                    class="w3-input w3-topbar" :class="selectorBorderClass" name="">
                    <option value="PRIVATE">Private (only I can see it)</option>
                    <option value="SHARED">Shared (others can see it, but its mine)</option>
                    <option value="COMMON">Common (controlled by all editors)</option>
                  </select>
                </div>

                <div v-if="!editing" class="">
                  <div v-if="card.imageFile" class="w3-row w3-margin-top image-container w3-center w3-display-container w3-card-2 cursor-pointer">
                    <img @click="showImageClick()" :src="card.imageFile.url + '?lastUpdated='+ card.imageFile.lastUpdated" alt="">
                  </div>
                </div>
                <div v-if="editing" class="">
                  <div class="w3-row w3-margin-top image-container w3-center w3-display-container w3-card-2">
                    <div v-if="uploadingImage" class="loader-gif-container">
                      <img class="loader-gif" src="../../../assets/loading.gif" alt="">
                    </div>
                    <div v-if="!uploadingImage" class="">
                      <img v-if="editedCard.imageFile" @click="showImageClick()" :src="editedCard.imageFile.url + '?lastUpdated='+ editedCard.imageFile.lastUpdated" alt="">
                    </div>
                    <div v-if="!uploadingImage" class="w3-display-middle">
                      <input class="inputfile" @change="newFileSelected($event)"
                        type="file" name="imageFile" id="imageFile" accept="image/*">
                      <label for="imageFile" class="w3-button app-button">{{ editedCard.imageFile ? 'change' : 'upload image' }}</label>
                      <button v-if="editedCard.imageFile"
                        @click="removeImage()"
                        class="w3-button app-button-danger">remove</button>
                    </div>
                  </div>
                  <app-error-panel
                    :show="errorUploadingFile"
                    :message="errorUploadingFileMsg">
                  </app-error-panel>
                </div>

                <div class="slider-container">
                  <transition name="slideDownUp">
                    <div v-if="showImage" class="image-display-modal">
                      <div class="image-display w3-display-container">
                        <img v-if="card.imageFile"
                          :src="card.imageFile.url + '?lastUpdated='+ card.imageFile.lastUpdated" alt="">
                        <div class="w3-display-topright w3-xlarge cursor-pointer close-image-div" @click="showImage = false">
                          <i class="fa fa-times" aria-hidden="true"></i>
                        </div>
                      </div>
                    </div>
                  </transition>
                </div>

                <div class="w3-row w3-margin-top">
                  <div v-if="!editing" class="">
                    <div v-if="card.title !== ''" class="">
                      <h3><b>{{ card.title }}</b></h3>
                    </div>
                    <div v-else class="">
                    </div>
                  </div>
                  <div v-else class="">
                    <label class=""><b>Title:</b></label>
                    <input type="text" class="w3-input w3-hover-light-grey" v-model="editedCard.title">
                    <app-error-panel
                      :show="titleEmptyShow"
                      message="please add a title">
                    </app-error-panel>
                    <app-error-panel
                      :show="titleTooLongShow"
                      message="name too long">
                    </app-error-panel>
                  </div>
                </div>

                <div class="w3-row">
                  <div v-if="!editing" class="">
                    <vue-markdown v-if="card.text" class="marked-text" :source="card.text" :anchorAttributes="{target: '_blank'}"></vue-markdown>
                  </div>
                  <div v-else class="w3-margin-top">
                    <label class=""><b>Text:</b></label>
                    <app-markdown-editor v-model="editedCard.text"></app-markdown-editor>
                    <app-error-panel
                      :show="textErrorShow"
                      message="please include the text of this card">
                    </app-error-panel>
                  </div>
                </div>

                <hr>

                <div v-if="!editing" class="">
                  <div class="w3-row">
                    <i>created in {{ dateString(cardWrapper.creationDate) }} by</i>
                    <div class="inline-block">
                      <app-user-avatar :user="cardWrapper.creator" :showName="true" :small="true"></app-user-avatar>
                    </div>
                  </div>
                  <div v-if="editors.length > 0" class="w3-row">
                    <i>edited by
                      <div class="inline-block" v-for="editor in editors">
                        <app-user-avatar v-if="editor.c1Id !== cardWrapper.creator.c1Id" :user="editor" :showName="true" :small="true"></app-user-avatar>
                      </div>
                    </i>
                  </div>
                </div>
              </div>
            </transition>
          </div>

          <br>
          <div v-if="!isNew && !editing" class="message-thread-container">
            <app-message-thread
              contextType="MODEL_CARD"
              :contextElementId="cardWrapperId"
              :contextOfContextElementId="inSectionId"
              :isFlex="false"
              :showMessages="true"
              :showEvents="true">
            </app-message-thread>
          </div>

          <div v-if="editing || addExisting" class="modal-bottom-btns-row w3-row-padding">
            <hr>
            <div class="w3-col m6">
              <button type="button" class="w3-button app-button-light" @click="closeThis()">Cancel <span><small>(Esc)</small></span></button>
            </div>
            <div class="w3-col m6 w3-center">
              <button v-show="!sendingData" type="button" class="w3-button app-button" @click="accept()">Accept <span><small>(Ctr + Enter)</small></span></button>
              <div v-show="sendingData" class="sending-accept light-grey">
                <img class="" src="../../../assets/loading.gif" alt="">
              </div>
            </div>
          </div>

        </div>
        <div v-else class="w3-row w3-center loader-gif-container">
          <img class="loader-gif" src="../../../assets/loading.gif" alt="">
        </div>

        <div v-if="closeIntent" class="w3-display-middle w3-card w3-white w3-padding w3-round-large w3-center">
          You are currently editing this card. Are you sure you want to close it? Any changes would get lost.
          <div class="w3-row w3-margin-top">
            <button class="w3-button app-button-light" name="button"
              @click="closeIntent = false">
              Cancel
            </button>
            <button class="w3-button app-button" name="button"
              @click="closeThis()">
              Confirm
            </button>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import { dateString } from '@/lib/common.js'
import ModelModalButtons from '@/components/model/modals/ModelModalButtons.vue'
import ModelCardSelector from '@/components/model/cards/ModelCardSelector.vue'
import ModelSectionSelector from '@/components/model/ModelSectionSelector.vue'
import MessageThread from '@/components/notifications/MessageThread.vue'

export default {

  components: {
    'app-model-modal-buttons': ModelModalButtons,
    'app-model-card-selector': ModelCardSelector,
    'app-message-thread': MessageThread,
    'app-model-section-selector': ModelSectionSelector
  },

  props: {
    isNew: {
      type: Boolean,
      default: false
    },
    addExistingInit: {
      type: Boolean,
      default: false
    },
    cardWrapperId: {
      type: String,
      default: ''
    },
    inSectionId: {
      type: String,
      default: ''
    },
    inSectionTitle: {
      type: String,
      default: ''
    },
    ixInSection: {
      type: Number,
      default: -1
    },
    editingInit: {
      type: Boolean,
      defaul: false
    },
    newCardLocation: {
      type: String,
      default: ''
    },
    atCardWrapper: {
      type: Object,
      default: null
    }
  },

  data () {
    return {
      cardWrapper: {
        id: '',
        creationDate: 0,
        card: {
          title: '',
          text: ''
        }
      },
      editedCard: null,
      editing: false,
      showEditButtons: false,
      titleEmptyError: false,
      textEmptyError: false,
      addExisting: false,
      existingCard: null,
      existingCardNewScope: '',
      noCardSelectedError: false,
      animatingTab: false,
      showUrl: false,
      showImage: false,
      imageUpdated: false,
      newImageFileId: '',
      uploadingImage: false,
      errorUploadingFile: false,
      errorUploadingFileMsg: '',
      enableClickOutside: false,
      sendingData: false,
      loading: false,
      closeIntent: false
    }
  },

  computed: {
    selectorExistingBorderClass () {
      let cClass = {}
      switch (this.existingCardNewScope) {
        case 'PRIVATE':
          cClass['border-red'] = true
          break

        case 'SHARED':
          cClass['border-yellow'] = true
          break

        default:
          cClass['border-blue'] = true
          break
      }
      return cClass
    },
    selectorBorderClass () {
      let cClass = {}
      switch (this.editedCard.newScope) {
        case 'PRIVATE':
          cClass['border-red'] = true
          break

        case 'SHARED':
          cClass['border-yellow'] = true
          break

        default:
          cClass['border-blue'] = true
          break
      }
      return cClass
    },
    isLoggedTheAuthor () {
      if (this.cardWrapper.creator && this.$store.state.user.profile) {
        console.log(this.cardWrapper)
        console.log(this.$store.state.user.profile)
        return this.cardWrapper.creator.c1Id === this.$store.state.user.profile.c1Id
      }
    },
    canChangeScope () {
      return this.isNew
    },
    showSelectFile () {
      return this.editing && this.changeImage
    },
    isLoggedAnEditor () {
      return this.$store.getters.isLoggedAnEditor
    },
    card () {
      return this.cardWrapper.card
    },
    editors () {
      var editors = []
      for (var ix in this.cardWrapper.editors) {
        if (this.cardWrapper.editors[ix].c1Id !== this.cardWrapper.creator.c1Id) {
          editors.push(this.cardWrapper.editors[ix])
        }
      }
      return editors
    },
    titleEmpty () {
      if (this.editedCard) {
        return this.editedCard.title === ''
      }
      return false
    },
    titleTooLong () {
      if (this.editedCard) {
        return this.editedCard.title.length > 42
      }
      return false
    },
    titleErrorShow () {
      return this.titleEmptyShow || this.titleTooLongShow
    },
    titleEmptyShow () {
      return this.titleEmptyError && this.titleEmpty
    },
    titleTooLongShow () {
      return this.titleTooLong
    },
    textEmpty () {
      if (this.editedCard) {
        return this.editedCard.text === ''
      }
      return false
    },
    textErrorShow () {
      return this.textEmptyError && this.textEmpty
    },
    noCardSelected () {
      return this.existingCard === null
    },
    noCardSelectedShow () {
      return this.noCardSelectedError && this.noCardSelected
    }
  },

  methods: {
    copyUrl () {
      if (!this.showUrl) {
        this.showUrl = true
        this.$nextTick(() => {
          var copyTextarea = document.querySelector('#copy-url')
          copyTextarea.select()
          document.execCommand('copy')
          console.log('copied')
        })
      } else {
        this.showUrl = false
      }
    },
    newFileSelected (event) {
      /* upload image */
      var fileData = event.target.files[0]
      if (fileData.size > 1048576) {
        this.errorUploadingFile = true
        this.errorUploadingFileMsg = 'Image file too big. Must be below 1 MB'
        return
      }

      var data = new FormData()
      data.append('file', fileData)

      this.uploadingImage = true
      this.errorUploadingFile = false

      this.axios.post('/1/upload/cardImage/' + this.cardWrapper.id, data).then((response) => {
        console.log(response)
        this.uploadingImage = false

        if (response.data.result === 'success') {
          this.editedCard.newImageFileId = response.data.elementId
          return this.axios.get('/1/files/' + this.editedCard.newImageFileId)
        } else {
          this.errorUploadingFile = true
          this.errorUploadingFileMsg = response.data.message
        }
      }).then((response) => {
        /* to force reactivity */
        var newEditedCard = JSON.parse(JSON.stringify(this.editedCard))
        newEditedCard.imageFile = response.data.data
        this.editedCard = newEditedCard
      })
    },
    dateString (v) {
      return dateString(v)
    },
    update () {
      this.loading = true
      this.axios.get('/1/model/cardWrapper/' + this.cardWrapper.id, {
        params: {
          inSectionId: this.inSectionId
        }
      }).then((response) => {
        this.cardWrapper = response.data.data
        this.loading = false
        if (this.editingInit) {
          this.startEditing()
        }
      })
    },
    cardSelected (cardWrapper) {
      if (cardWrapper) {
        this.existingCard = cardWrapper
        this.existingCardNewScope = 'COMMON'
      }
    },
    clickOutside () {
      if (this.enableClickOutside) {
        this.closeThisConfirm()
      }
    },
    closeThisConfirm () {
      if (this.editing && (!this.titleEmpty || !this.textEmpty)) {
        this.closeIntent = true
      } else {
        this.closeThis()
      }
    },
    closeThis () {
      this.$emit('close')
    },
    cancel () {
      if (this.isNew) {
        this.closeThis()
      } else {
        this.editing = false
      }
    },
    startEditing () {
      this.editedCard = JSON.parse(JSON.stringify(this.card))
      this.editedCard.newScope = this.cardWrapper.scope
      this.editing = true
    },
    accept () {
      // console.log('clicked accept')
      var ok = true

      if (!this.addExisting) {
        if (this.titleTooLong) {
          ok = false
        }
      } else {
        if (this.noCardSelected) {
          ok = false
          this.noCardSelectedError = true
        }
      }

      if (ok) {
        var cardDto = JSON.parse(JSON.stringify(this.editedCard))

        if (this.isNew) {
          if (!this.addExisting) {
            /* create new card */
            this.sendingData = true
            this.axios.post('/1/model/section/' + this.inSectionId + '/cardWrapper', cardDto, { params: {
              onCardWrapperId: this.atCardWrapper ? this.atCardWrapper.id : null,
              isBefore: this.newCardLocation === 'before'
            } }).then((response) => {
              this.sendingData = false
              if (response.data.result === 'success') {
                this.closeThis()
                this.$emit('updateCards')
              } else {
                console.log(response.data.message)
              }
            }).catch((error) => {
              console.log(error)
            })
          } else {
            this.sendingData = true
            this.axios.put('/1/model/section/' + this.inSectionId + '/cardWrapper/' + this.existingCard.id,
              {}, {
                params: {
                  onCardWrapperId: this.atCardWrapper ? this.atCardWrapper.id : null,
                  isBefore: this.newCardLocation === 'before',
                  scope: this.existingCardNewScope
                }
              }).then((response) => {
              this.sendingData = false
              if (response.data.result === 'success') {
                this.closeThis()
                this.$emit('updateCards')
              }
            }).catch((error) => {
              console.log(error)
            })
          }
        } else {
          /* editing */
          this.sendingData = true
          this.axios.put('/1/model/cardWrapper/' + this.cardWrapper.id, cardDto, {
            params: {
              inSectionId: this.inSectionId
            }
          })
            .then((response) => {
              this.sendingData = false
              if (response.data.result === 'success') {
                this.closeThis()
                this.$emit('update')
              }
            }).catch((error) => {
              console.log(error)
            })
        }
      }
    },
    removeCard () {
      var responseF = (response) => {
        if (response.data.result === 'success') {
          this.closeThis()
          this.$store.commit('triggerUpdateSectionCards')
        } else {
          this.showOutputMessage(response.data.message)
        }
      }
      this.axios.put('/1/model/section/' + this.inSectionId + '/removeCard/' + this.cardWrapper.id,
        {}).then(responseF).catch((error) => {
        console.log(error)
      })
    },
    deleteCard () {
      this.axios.delete('/1/model/cardWrapper/' + this.cardWrapper.id).then((response) => {
        this.$store.commit('triggerUpdateSectionCards')
        this.closeThis()
      }).catch((error) => {
        console.log(error)
      })
    },
    showImageClick () {
      if (!this.editing) {
        this.showImage = true
      }
    },
    removeImage () {
      this.editedCard.imageFile = null
      this.editedCard.newImageFileId = 'REMOVE'
    },
    atKeydown (e) {
      if (!this.editing) {
        /* esc */
        if (e.keyCode === 27) {
          this.closeThis()
        }
      }

      if (this.editing) {
        /* esc */
        if (e.keyCode === 27) {
          this.cancel()
        }

        /* ctr + enter */
        if (e.keyCode === 13 && e.ctrlKey) {
          e.preventDefault()
          this.accept()
        }
      }
    }
  },

  mounted () {
    if (this.isNew) {
      this.editedCard = {
        title: '',
        text: '',
        newScope: 'COMMON'
      }
      this.editing = true
      this.addExisting = this.addExistingInit
    } else {
      this.showEditButtons = true
      this.cardWrapper.id = this.cardWrapperId
      this.update()
    }

    setTimeout(() => {
      this.enableClickOutside = true
    }, 1000)

    window.addEventListener('keydown', this.atKeydown)
  },

  destroyed () {
    window.removeEventListener('keydown', this.atKeydown)
  }
}
</script>

<style scoped>

.close-btn {
  border-top-right-radius: 20px;
}

.not-add-existing-container {
  overflow: visible;
}

.inputfile {
  width: 0.1px;
  height: 0.1px;
  opacity: 0;
  overflow: hidden;
  position: absolute;
  z-index: -1;
}

.loader-gif-container {
  padding-top: 30px;
  padding-bottom: 30px;
}

.insection-tag-container {
  margin-left: 6px;
}

.image-container {
  min-height: 80px;
  max-height: 250px;
  overflow: hidden;
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
}

.image-container img {
  max-height: 100%;
  max-width: 100%;
}

.image-display-modal {
  position: fixed;
  top: 5%;
  left: 5%;
  width: 90%;
  height: 90%;
  text-align: center;
  background-color: white;
  border-radius: 20px;
  z-index: 10;
}

.image-display {
  width: 100%;
  height: 100%;
  padding: 10px;
}

.image-display img {
  zoom: 2;  //increase if you have very small images
  display: block;
  margin: auto;
  height: auto;
  max-height: 100%;
  width: auto;
  max-width: 100%;
  border-radius: 5px;
}

.close-image-div {
  background-color: rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  width: 50px;
  margin-top: -10px;
  margin-right: -10px;
  text-align: center;
}

.state-enable-button {
  width: 250px;
}

.state-tag {
  width: 100%;
}

.remove-tag-icon {
  margin-top: -5px;
  margin-right: -10px;
  z-index: 10;
}

.message-thread-container {
}

</style>
