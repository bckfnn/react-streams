/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Core react-stream classes.
 * Type identifiers used are 
 * <ul>
 * <li> {@code <T>} for the output type of a {@link io.github.bckfnn.reactstreams.Stream}.</li>
 * <li> {@code <I>} for the input types in processing pipes.</li>
 * <li> {@code <O>} for the output types in processing pipes and operations.</li>
 * <li> {@code <R>} for return types in functional interfaces.
 * <li> {@code <A>} for argument types in functional interfaces.</li>
 * <li> {@code <S>} throw away types to handle generic returns, {@link io.github.bckfnn.reactstreams.Stream#chain(org.reactivestreams.Subscriber)}.</li>
 * </ul> 
 */
package io.github.bckfnn.reactstreams;

